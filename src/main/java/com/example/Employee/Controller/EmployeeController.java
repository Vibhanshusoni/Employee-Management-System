package com.example.Employee.Controller;

import com.example.Employee.Dto.*;
import com.example.Employee.Entity.User;
import com.example.Employee.Service.DashboardService;
import com.example.Employee.Service.EmployeeService;
import com.example.Employee.Service.LoginService;
import com.example.Employee.Service.ServiceImpl.AuthServiceImpl;
import com.example.Employee.Service.UserAdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@SecurityRequirement(name = "BearerAuth")
public class EmployeeController {

    private static final Logger logger = LoggerFactory.getLogger(EmployeeController.class);
    private final EmployeeService employeeService;
    private final DashboardService dashboardService;
    private final LoginService loginService;
    private final AuthServiceImpl authService;
    private final UserAdminService userAdminService;

    /*---------------------------------------------LOGIN---------------------------------------------*/
    @Operation(summary = "User Login", description = "Authenticate user and generate JWT tokens")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Login successful"),
            @ApiResponse(responseCode = "401", description = "Invalid credentials")
    })
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {

        logger.info("Login request received for {}", request.getUsername());

        AuthResponse response = loginService.login(request);

        return ResponseEntity.ok(response);
    }

    /*---------------------------------------------REFRESH TOKEN---------------------------------------------*/
    @Operation(summary = "Refresh JWT Token")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Token refreshed successfully"),
            @ApiResponse(responseCode = "401", description = "Invalid refresh token")
    })
    @PreAuthorize("permitAll()")
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@RequestBody RefreshRequest request) {

        logger.info("Refresh token request received");

        AuthResponse response = authService.refreshToken(request.getRefreshToken());

        return ResponseEntity.ok(response);
    }

    /*---------------------------------------------TEMP BLOCKED USERS---------------------------------------------*/

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/temp-blocked")
    public ResponseEntity<?> getTemporaryBlockedUsers() {

        logger.info("Admin requested temporary blocked users");
        List<User> users = userAdminService.getTemporaryBlockedUsers();

        if (users.isEmpty()) {
            return ResponseEntity.ok("No user is Temporarily blocked");
        }

        return ResponseEntity.ok(userAdminService.getTemporaryBlockedUsers());
    }
    /*---------------------------------------------Permanent BLOCKED USERS---------------------------------------------*/

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/permanent-blocked")
    public ResponseEntity<?> getPermanentBlockedUsers() {

        logger.info("Admin requested permanently blocked users list");

        List<User> users = userAdminService.getPermanentBlockedUsers();

        if (users.isEmpty()) {
            return ResponseEntity.ok("No user is permanently blocked");
        }

        return ResponseEntity.ok(users);
    }
    /*---------------------------------------------PERMANENT BLOCK---------------------------------------------*/

    @PostMapping("/admin/block/{id}")
    public ResponseEntity<String> blockUser(@PathVariable Long id) {

        logger.warn("Admin blocking user {}", id);

        userAdminService.blockUser(id);

        return ResponseEntity.ok("User permanently blocked");
    }

    /*---------------------------------------------PERMANENT BLOCK By User---------------------------------------------*/

    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    @PostMapping("/self-block")
    public ResponseEntity<String> selfBlock(Authentication authentication) {

        String username = authentication.getName();

        logger.warn("User {} requested self permanent block", username);

        userAdminService.selfBlock(username);

        return ResponseEntity.ok("Your account has been permanently blocked and can only be unblocked by admin");
    }
    /*---------------------------------------------UNBLOCK---------------------------------------------*/

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/unblock/{id}")
    public ResponseEntity<String> unblockUser(@PathVariable Long id) {

        logger.info("Admin attempting to unblock user {}", id);

        userAdminService.unblockUser(id);

        return ResponseEntity.ok("User unblocked successfully by Admin");
    }

    /*---------------------------------------------LOGOUT---------------------------------------------*/

    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpServletRequest request) {

        logger.info("Logout request received");

        authService.logout(request);

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