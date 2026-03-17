package com.example.Employee.Service;

import com.example.Employee.Dto.AuthResponse;
import com.example.Employee.Dto.LoginRequest;

public interface LoginService {
    AuthResponse login(LoginRequest request);
