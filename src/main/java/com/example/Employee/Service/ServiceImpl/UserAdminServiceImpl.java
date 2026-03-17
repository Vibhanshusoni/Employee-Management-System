package com.example.Employee.Service.ServiceImpl;

import com.example.Employee.Entity.BlockType;
import com.example.Employee.Entity.User;
import com.example.Employee.Exceptions.EmployeeNotFoundException;
import com.example.Employee.Repository.UserRepository;
import com.example.Employee.Service.UserAdminService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserAdminServiceImpl implements UserAdminService {

    private final UserRepository userRepository;

    public List<User> getTemporaryBlockedUsers() {
        log.info("Fetching temporary blocked users");
        return userRepository.findByBlockedTrueAndBlockType(BlockType.TEMPORARY);
    }

    public List<User> getPermanentBlockedUsers() {
        log.info("Fetching permanent blocked users");
        return userRepository.findByBlockedTrueAndBlockType(BlockType.PERMANENT);
    }

    public void blockUser(Long id) {

        User user = userRepository.findById(id)
                .orElseThrow(() -> new EmployeeNotFoundException("User not found"));

        user.setEnabled(false);
        user.setBlocked(true);
        user.setBlockType(BlockType.PERMANENT);

        userRepository.save(user);

        log.warn("User {} permanently blocked", id);
    }

    public void unblockUser(Long id) {

        User user = userRepository.findById(id)
                .orElseThrow(() -> new EmployeeNotFoundException("User not found"));

        user.setEnabled(true);
        user.setBlocked(false);
        user.setFailedAttempts(0);
        user.setBlockType(null);
        user.setLockoutTime(null);

        userRepository.save(user);

        log.info("User {} unblocked", id);
    }

    public void selfBlock(String username) {

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new EmployeeNotFoundException("User not found"));

        user.setEnabled(false);
        user.setBlocked(true);
        user.setBlockType(BlockType.PERMANENT);

        userRepository.save(user);

        log.warn("User {} self-blocked account", username);
    }
}