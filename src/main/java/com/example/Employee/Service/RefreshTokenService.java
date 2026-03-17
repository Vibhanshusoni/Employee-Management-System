package com.example.Employee.Service;

import com.example.Employee.Entity.RefreshToken;

public interface RefreshTokenService {
    String createRefreshToken(String username);

    RefreshToken validate(String token);

    RefreshToken rotateToken(RefreshToken oldToken);

}