package com.example.Employee.Service;

import com.example.Employee.Entity.User;

import java.util.List;

public interface UserAdminService {
    List<User> getTemporaryBlockedUsers();

    List<User> getPermanentBlockedUsers();

    void blockUser(Long id);

    void unblockUser(Long id);

    void selfBlock(String username);
}
