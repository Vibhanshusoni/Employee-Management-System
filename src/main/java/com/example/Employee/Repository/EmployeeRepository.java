package com.example.Employee.Repository;

import com.example.Employee.Entity.Employee;

import java.util.List;
import java.util.Optional;

public interface EmployeeRepository {

    Employee save(Employee emp);

    List<Employee> findAll();

    Optional<Employee> findById(Long id);

    Optional<Employee> findByEmail(String email);

    Optional<Employee> findByUserUsername(String username);
    long countByDepartment(String department);

    void deleteById(Long id);

    long count();
}
