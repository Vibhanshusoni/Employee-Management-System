package com.example.Employee.Controller;

import com.example.Employee.Dto.*;
import com.example.Employee.Entity.BlockType;
import com.example.Employee.Entity.RefreshToken;
import com.example.Employee.Entity.User;
import com.example.Employee.Exceptions.EmployeeNotFoundException;
import com.example.Employee.Repository.UserRepository;
import com.example.Employee.Service.CustomUserDetailsService;
import com.example.Employee.Service.EmployeeService;
import com.example.Employee.Service.RefreshTokenService;
import com.example.Employee.Service.ServiceImpl.DashboardService;
import com.example.Employee.Service.TokenBlacklistService;
import com.example.Employee.config.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class EmployeeController {

    private static final Logger logger = LoggerFactory.getLogger(EmployeeController.class);
    private final EmployeeService employeeService;
    private final DashboardService dashboardService;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final CustomUserDetailsService userDetailsService;
    private final RefreshTokenService refreshTokenService;
    private final TokenBlacklistService tokenBlacklist;
    private final UserRepository userRepository;

    /*---------------------------------------------LOGIN---------------------------------------------*/

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {

        logger.info("Login request received for username: {}", request.getUsername());

        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> {
                    logger.error("User not found: {}", request.getUsername());
                    return new RuntimeException("User not found");
                });

        logger.debug("User record found in database for {}", user.getUsername());

        if (!user.isEnabled()) {
            logger.warn("User {} is permanently blocked", user.getUsername());
            throw new RuntimeException("Account permanently blocked");
        }

        if (user.getBlockType() == BlockType.PERMANENT) {
            throw new RuntimeException("Account permanently blocked");
        }

        if (user.isBlocked() && user.getBlockType() == BlockType.TEMPORARY) {

            if (user.getLockoutTime() != null &&
                    user.getLockoutTime().isBefore(LocalDateTime.now())) {

                logger.info("Temporary lock expired for {}", user.getUsername());

                user.setBlocked(false);
                user.setFailedAttempts(0);
                user.setBlockType(null);
                user.setLockoutTime(null);

                userRepository.save(user);

            }

            logger.warn("User {} is currently blocked", user.getUsername());

            if (user.getLockoutTime() != null &&
                    user.getLockoutTime().isAfter(LocalDateTime.now())) {

                long minutesLeft =
                        Duration.between(LocalDateTime.now(), user.getLockoutTime()).toMinutes();

                logger.error("User {} is temporarily locked for {} minutes", user.getUsername(), minutesLeft);

                throw new RuntimeException(
                        "Account locked. Try again after " + minutesLeft + " minutes"
                );
            }

            logger.info("Temporary block expired for user {}", user.getUsername());
        }

        logger.debug("Authenticating user {}", request.getUsername());

        Authentication authentication;

        try {

            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getUsername(),
                            request.getPassword()
                    )
            );

        } catch (BadCredentialsException ex) {

            user.setFailedAttempts(user.getFailedAttempts() + 1);

            if (user.getFailedAttempts() >= 5) {

                user.setBlocked(true);
                user.setBlockType(BlockType.TEMPORARY);
                user.setLockoutTime(LocalDateTime.now().plusMinutes(30));

                logger.error("User {} locked due to too many attempts", user.getUsername());

            }

            userRepository.save(user);

            throw ex;
        }

        user.setFailedAttempts(0);
        userRepository.save(user);

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();

        logger.info("Authentication successful for user {}", userDetails.getUsername());

        logger.debug("Generating access token for user {}", userDetails.getUsername());

        String accessToken = jwtUtil.generateToken(userDetails);

        logger.debug("Generating refresh token for user {}", userDetails.getUsername());

        String refreshToken = refreshTokenService.createRefreshToken(userDetails.getUsername());

        Date expiryDate = jwtUtil.extractExpiration(accessToken);

        logger.info("Tokens generated successfully for user {}", userDetails.getUsername());

        AuthResponse response = new AuthResponse(
                accessToken,
                refreshToken,
                userDetails.getUsername(),
                LocalDateTime.now(),
                expiryDate.toInstant()
                        .atZone(ZoneId.systemDefault())
                        .toLocalDateTime()
        );

        return ResponseEntity.ok(response);
    }

    /*---------------------------------------------REFRESH TOKEN---------------------------------------------*/
    @PreAuthorize("permitAll()")
    @PostMapping("/refresh")
    public AuthResponse refresh(@RequestBody RefreshRequest request) {

        logger.info("Refresh token request received");

        if (request.getRefreshToken() == null || request.getRefreshToken().isEmpty()) {
            logger.error("Refresh token missing in request");
            throw new RuntimeException("Refresh token required");
        }

        logger.debug("Validating refresh token");

        RefreshToken oldToken = refreshTokenService.validate(request.getRefreshToken());

        logger.info("Refresh token validated for user {}", oldToken.getUsername());

        logger.debug("Rotating refresh token");

        RefreshToken newToken = refreshTokenService.rotateToken(oldToken);

        logger.info("Refresh token rotated successfully");

        logger.debug("Loading user details for {}", oldToken.getUsername());

        UserDetails userDetails =
                userDetailsService.loadUserByUsername(oldToken.getUsername());

        logger.debug("Generating new access token");

        String newAccessToken = jwtUtil.generateToken(userDetails);

        logger.info("New access token generated for {}", oldToken.getUsername());

        return new AuthResponse(
                newAccessToken,
                newToken.getToken(),
                oldToken.getUsername(),
                LocalDateTime.now(),
                jwtUtil.extractExpiration(newAccessToken)
                        .toInstant()
                        .atZone(ZoneId.systemDefault())
                        .toLocalDateTime()
        );
    }

    /*---------------------------------------------TEMP BLOCKED USERS---------------------------------------------*/

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/temp-blocked")
    public ResponseEntity<?> getTemporaryBlockedUsers() {

        logger.info("Admin requested temporary blocked users list");

        List<User> users = userRepository.findByBlockedTrueAndBlockType(BlockType.TEMPORARY);

        if (users.isEmpty()) {

            logger.info("No temporarily blocked users found");

            return ResponseEntity.ok("No User is Temporary Blocked");
        }

        logger.info("Found {} temporarily blocked users", users.size());

        return ResponseEntity.ok(users);
    }
    /*---------------------------------------------Permanent BLOCKED USERS---------------------------------------------*/

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/permanent-blocked")
    public ResponseEntity<?> getPermanentBlockedUsers() {

        logger.info("Admin requested Permanent blocked users list");

        List<User> users = userRepository.findByBlockedTrueAndBlockType(BlockType.PERMANENT);
        if (users.isEmpty()) {

            logger.info("No permanently blocked users found");

            return ResponseEntity.ok("No User is permanently Blocked");
        }
        logger.info("Found {} Permanent blocked users", users.size());

        return ResponseEntity.ok(users);

    }

    /*---------------------------------------------PERMANENT BLOCK---------------------------------------------*/

    @PostMapping("/admin/block/{id}")
    public ResponseEntity<String> blockUser(@PathVariable Long id) {

        logger.warn("Admin attempting to permanently block user {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> {
                    logger.error("User {} not found for blocking", id);
                    return new EmployeeNotFoundException("User with id " + id + " not found");
                });

        user.setEnabled(false);
        user.setBlocked(true);
        user.setBlockType(BlockType.PERMANENT);

        userRepository.save(user);

        logger.warn("User {} permanently blocked", id);
        return ResponseEntity.ok("User's account has been permanently blocked Only can be unblocked by you");
    }

    /*---------------------------------------------PERMANENT BLOCK By User---------------------------------------------*/

    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    @PostMapping("/self-block")
    public ResponseEntity<String> selfBlock(Authentication authentication) {

        String username = authentication.getName();

        logger.warn("User {} requested self permanent block", username);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setEnabled(false);
        user.setBlocked(true);
        user.setBlockType(BlockType.PERMANENT);

        userRepository.save(user);

        logger.warn("User {} permanently blocked himself", username);

        return ResponseEntity.ok("Your account has been permanently blocked and can only be unblocked by admin");
    }

    /*---------------------------------------------UNBLOCK---------------------------------------------*/

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/unblock/{id}")
    public ResponseEntity<String> unblockUser(@PathVariable Long id) {

        logger.info("Admin attempting to unblock user {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> {
                    logger.error("User {} not found for unblocking", id);
                    return new EmployeeNotFoundException("User with id " + id + " not found");
                });

        user.setEnabled(true);
        user.setBlocked(false);
        user.setFailedAttempts(0);
        user.setBlockType(null);
        user.setLockoutTime(null);

        userRepository.save(user);

        logger.info("User {} successfully unblocked", id);
        return ResponseEntity.ok("User unblocked successfully by Admin");
    }

    /*---------------------------------------------LOGOUT---------------------------------------------*/

    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpServletRequest request) {

        logger.info("Logout request received");

        String token = JwtUtil.extractToken(request);

        if (token == null || token.isEmpty()) {
            logger.warn("Logout failed. Token missing");
            return ResponseEntity.badRequest().body("Token missing");
        }

        logger.debug("Adding token to blacklist");

        tokenBlacklist.add(token);

        logger.info("Token successfully blacklisted");

        return ResponseEntity.ok("Logged out successfully");
    }

    /*---------------------------------------------CreateEmployee---------------------------------------------*/

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<EmployeeDto> save(@Valid @RequestBody EmployeeDto emp, Authentication authentication) {

        String adminUsername = authentication.getName();

        logger.info("Admin {} creating employee {}", adminUsername, emp.getUsername());

        EmployeeDto savedEmployee = employeeService.createEmployee(emp);

        logger.info("Employee created successfully {}", savedEmployee.getEmail());

        return ResponseEntity.ok(savedEmployee);
    }

    /*---------------------------------------------GetEmployeeById---------------------------------------------*/

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("employee/{id}")
    public ResponseEntity<EmployeeDto> getEmployeeById(@PathVariable Long id) {

        logger.info("Fetching employee by id {}", id);

        EmployeeDto employee = employeeService.getEmployeeById(id);

        return ResponseEntity.ok(employee);
    }

    /*---------------------------------------------GetOwnData---------------------------------------------*/

    @PreAuthorize("permitAll()")
    @GetMapping("/me")
    public ResponseEntity<UserDto> getMyProfile(Authentication authentication) {

        logger.debug("Fetching profile for {}", authentication.getName());

        UserDto employee = employeeService.getUserByUsername(authentication.getName());

        logger.info("Profile fetched for {}", authentication.getName());

        return ResponseEntity.ok(employee);
    }

    /*---------------------------------------------GetAllEmployees---------------------------------------------*/

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<List<EmployeeDto>> getAllEmployee() {

        logger.info("Fetching all employees");

        List<EmployeeDto> employee = employeeService.getAllEmployee();

        logger.info("Total employees fetched {}", employee.size());

        return ResponseEntity.ok(employee);
    }

    /*---------------------------------------------GetAllUsers---------------------------------------------*/

    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserDto>> getAllUsers() {

        return ResponseEntity.ok(employeeService.getAllUsers());
    }

    /*---------------------------------------------Dashboard---------------------------------------------*/

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/dashboard")
    public DashboardDto getDashboard() {

        logger.info("Admin requested dashboard data");

        return dashboardService.getDashboardData();
    }

    /*---------------------------------------------UpdateUser---------------------------------------------*/

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<EmployeeDto> updateEmployee(@PathVariable Long id, @RequestBody EmployeeDto employeeDto) {

        logger.info("Updating employee {}", id);

        EmployeeDto update = employeeService.updateEmployee(id, employeeDto);

        logger.info("Employee {} updated successfully", id);

        return ResponseEntity.ok(update);
    }

    /*---------------------------------------------DeleteUser---------------------------------------------*/

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteEmployee(@PathVariable Long id) {

        logger.warn("Deleting employee {}", id);

        employeeService.deleteEmployee(id);

        logger.warn("Employee {} deleted successfully", id);

        return ResponseEntity.ok("Employee deleted successfully");
    }
}