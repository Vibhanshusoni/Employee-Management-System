package com.example.Employee.Dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@AllArgsConstructor
@RequiredArgsConstructor
@Getter
@Setter
public class AuthResponse {
    private String token;
    private String refreshToken;
    private String username;
    private LocalDateTime timeStamp;
    private LocalDateTime expiryTime;
}
