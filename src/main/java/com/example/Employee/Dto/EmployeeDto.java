package com.example.Employee.Dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class EmployeeDto {

    private String name;
    @Email(message = "Invalid email format")
    @NotBlank(message = "Email cannot be empty")
    private String email;
    private String department;

    @NotBlank(message = "Username required")
    private String username;
    @NotBlank(message = "Password required")
    private String password;
}
