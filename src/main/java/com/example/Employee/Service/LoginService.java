package com.example.Employee.Service;

import com.example.Employee.Entity.User;
import com.example.Employee.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class LoginService {
    private final UserRepository userRepository;
    private static final Logger logger =
            LoggerFactory.getLogger(LoginService.class);

    public void loginSuccess(User user) {

        user.setFailedAttempts(0);
        user.setBlocked(false);
        user.setLockoutTime(null);

        userRepository.save(user);

        logger.info("Login success. Attempts reset for user {}", user.getUsername());
    }
}

