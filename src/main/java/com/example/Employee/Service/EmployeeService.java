package com.example.Employee.Service;

import com.example.Employee.Dto.EmployeeDto;
import com.example.Employee.Dto.UserDto;

import java.util.List;

public interface EmployeeService {
    EmployeeDto createEmployee(EmployeeDto employeeDto);

    EmployeeDto getEmployeeById(Long id);

    UserDto getUserByUsername(String username);

    EmployeeDto getEmployeeByEmail(String email);

    List<EmployeeDto> getAllEmployee();

    EmployeeDto updateEmployee(Long id, EmployeeDto employeeDto);

    void deleteEmployee(Long id);

    List<UserDto> getAllUsers();

}
