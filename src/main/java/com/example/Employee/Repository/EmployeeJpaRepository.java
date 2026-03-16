package com.example.Employee.Repository;

import com.example.Employee.Entity.Employee;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@Profile("Jpa")
public interface EmployeeJpaRepository extends JpaRepository<Employee, Long>, EmployeeRepository {

    Optional<Employee> findByEmail(String email);

    Optional<Employee> findByUserUsername(String username);

}
