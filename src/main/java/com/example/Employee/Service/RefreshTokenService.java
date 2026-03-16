package com.example.Employee.Service;


import com.example.Employee.Entity.RefreshToken;
import com.example.Employee.Repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository repo;
    private static final Logger logger =
            LoggerFactory.getLogger(RefreshTokenService.class);

    /*---------------------------------------------CreateRefresh---------------------------------------------*/

    public String createRefreshToken(String username) {

        logger.info("Creating refresh token for user {}", username);

        String token = "rt_" + UUID.randomUUID();

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setToken(token);
        refreshToken.setUsername(username);
        refreshToken.setExpiryDate(LocalDateTime.now().plusDays(7));
        refreshToken.setRevoked(false);

        repo.save(refreshToken);

        logger.info("Refresh token created successfully for user {}", username);

        return token;
    }

    /*---------------------------------------------ValidateRefresh---------------------------------------------*/


    public RefreshToken validate(String token) {

        logger.debug("Validating refresh token");

        RefreshToken refreshToken = repo.findByToken(token)
                .orElseThrow(() -> {
                    logger.error("Refresh token not found");
                    return new RuntimeException("Invalid refresh token");
                });

        if (refreshToken.isRevoked()) {
            logger.warn("Refresh token already revoked");
            throw new RuntimeException("Refresh token revoked");
        }

        if (refreshToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            logger.error("Refresh token expired");
            throw new RuntimeException("Refresh token expired");
        }

        logger.info("Refresh token validated successfully for user {}", refreshToken.getUsername());

        return refreshToken;
    }

    /*---------------------------------------------RotateRefreshToken---------------------------------------------*/

    public RefreshToken rotateToken(RefreshToken oldToken) {

        oldToken.setRevoked(true);
        repo.save(oldToken);

        RefreshToken newToken = new RefreshToken();

        newToken.setToken("rt_" + UUID.randomUUID());
        newToken.setUsername(oldToken.getUsername());
        newToken.setExpiryDate(LocalDateTime.now().plusDays(7));
        newToken.setRevoked(false);

        logger.info("Refresh token rotated for user {}", oldToken.getUsername());

        return repo.save(newToken);
    }

}