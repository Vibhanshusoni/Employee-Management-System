package com.example.Employee.Service.ServiceImpl;

import com.example.Employee.Dto.AuthResponse;
import com.example.Employee.Dto.LoginRequest;
import com.example.Employee.Entity.BlockType;
import com.example.Employee.Entity.User;
import com.example.Employee.Exceptions.EmployeeNotFoundException;
import com.example.Employee.Repository.UserRepository;
import com.example.Employee.Security.JwtUtil;
import com.example.Employee.Service.LoginService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

@Service
@RequiredArgsConstructor
public class LoginServiceImpl implements LoginService {

    private static final Logger logger =
            LoggerFactory.getLogger(LoginServiceImpl.class);

    private final UserRepository userRepository;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final RefreshTokenServiceImpl refreshTokenServiceImpl;

    public AuthResponse login(LoginRequest request) {


        logger.info("Login request received for username: {}", request.getUsername());

        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> {
                    logger.error("User not found: {}", request.getUsername());
                    throw new EmployeeNotFoundException("User not found");
                });

        logger.debug("User record found in database for {}", user.getUsername());

        if (!user.isEnabled()) {
            logger.warn("User {} is permanently blocked", user.getUsername());
            throw new IllegalArgumentException("Account permanently blocked");
        }

        if (user.getBlockType() == BlockType.PERMANENT) {
            throw new IllegalArgumentException("Account permanently blocked");
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

        } catch (Exception ex) {

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

        String refreshToken = refreshTokenServiceImpl.createRefreshToken(userDetails.getUsername());

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

        return response;
    }
}