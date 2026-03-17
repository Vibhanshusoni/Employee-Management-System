package com.example.Employee.Service.ServiceImpl;

import com.example.Employee.Entity.BlockType;
import com.example.Employee.Entity.User;
import com.example.Employee.Repository.UserRepository;
import com.example.Employee.Service.UserSecurityService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UserSecurityServiceImpl implements UserSecurityService {
    private final UserRepository userRepository;
    private static final Logger logger =
            LoggerFactory.getLogger(UserSecurityServiceImpl.class);
    private static final int MAX_ATTEMPTS = 5;

    public void loginFailed(User user) {

        int attempts = user.getFailedAttempts() + 1;

        user.setFailedAttempts(attempts);

        if (attempts >= MAX_ATTEMPTS) {

            user.setBlocked(true);
            user.setBlockType(BlockType.TEMPORARY);
            user.setLockoutTime(LocalDateTime.now().plusMinutes(30));

            logger.warn("User {} temporarily locked due to failed attempts", user.getUsername());
        }

        userRepository.save(user);
    }

    public void loginSuccess(User user) {

        user.setFailedAttempts(0);
        user.setBlocked(false);
        user.setLockoutTime(null);
        userRepository.save(user);

        logger.info("Login success. Attempts reset for user {}", user.getUsername());
    }
}

