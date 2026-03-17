package com.example.Employee.Service;

import com.example.Employee.Dto.AuthResponse;

import jakarta.servlet.http.HttpServletRequest;


public interface AuthService {
    public void logout(HttpServletRequest request);
    public AuthResponse refreshToken(String refreshToken) ;
}
