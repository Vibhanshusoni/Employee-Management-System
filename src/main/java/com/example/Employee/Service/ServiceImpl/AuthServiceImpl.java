package com.example.Employee.Service.ServiceImpl;

import com.example.Employee.Dto.AuthResponse;
import com.example.Employee.Entity.RefreshToken;
import com.example.Employee.Security.JwtUtil;
import com.example.Employee.Service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final RefreshTokenService refreshTokenService;
    private final CustomUserDetailsService userDetailsService;
    private final JwtUtil jwtUtil;
    private final TokenBlacklistServiceImpl tokenBlacklist;
    public void logout(HttpServletRequest request) {

        String token = JwtUtil.extractToken(request);

        if (token == null || token.isEmpty()) {
            throw new RuntimeException("Token missing");
        }

        tokenBlacklist.add(token);

        log.info("Token successfully blacklisted");
    }
    public AuthResponse refreshToken(String refreshToken) {

        log.info("Processing refresh token request");

        RefreshToken oldToken = refreshTokenServiceImpl.validate(refreshToken);
        RefreshToken newToken = refreshTokenServiceImpl.rotateToken(oldToken);

        UserDetails userDetails =
                userDetailsService.loadUserByUsername(oldToken.getUsername());

        String newAccessToken = jwtUtil.generateToken(userDetails);

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
}