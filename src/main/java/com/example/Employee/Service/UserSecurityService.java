package com.example.Employee.Service;

import com.example.Employee.Entity.User;

public interface UserSecurityService {

    void loginFailed(User user);

    void loginSuccess(User user);
}
