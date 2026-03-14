package com.example.Employee.Dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DashboardDto {
    private long totalEmployees;
    private long totalUsers;
    private long itDepartmentCount;
    private long hrDepartmentCount;
}
