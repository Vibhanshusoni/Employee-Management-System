package com.example.Employee.Dto;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor

public class RefreshRequest {
    private String refreshToken;

}
