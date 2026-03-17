package com.example.Employee.Service.ServiceImpl;

import com.example.Employee.Dto.EmployeeDto;
import com.example.Employee.Dto.UserDto;
import com.example.Employee.Entity.Employee;
import com.example.Employee.Entity.User;
import com.example.Employee.Exceptions.EmployeeNotFoundException;
import com.example.Employee.Exceptions.EmptyEmailException;
import com.example.Employee.Exceptions.InvalidEmailException;
import com.example.Employee.Exceptions.UsernameAlreadyExistsException;
import com.example.Employee.Repository.EmployeeRepository;
import com.example.Employee.Repository.UserRepository;
import com.example.Employee.Service.EmployeeService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EmployeeServiceImpl implements EmployeeService {

    private static final Logger logger =
            LoggerFactory.getLogger(EmployeeServiceImpl.class);

    private final EmployeeRepository employeeRepo;
    private final ModelMapper modelMapper;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    /*-------------------------------------------EmailValidation---------------------------------------------*/

    private boolean isValidEmail(String email) {
        String regex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";
        return email != null && email.matches(regex);
    }
    /*-------------------------------------------AddNewEmployee---------------------------------------------*/

    @Override
    public EmployeeDto createEmployee(EmployeeDto emp) {

        logger.info("Admin requested to create employee with username: {}", emp.getUsername());
        if (emp.getEmail() == null || emp.getEmail().isBlank()) {
            throw new EmptyEmailException("Email cannot be empty");
        }

        if (!isValidEmail(emp.getEmail())) {
            throw new InvalidEmailException("Invalid email format");
        }
        if (emp.getUsername() == null || emp.getPassword() == null) {
            logger.error("Username or password is missing for employee creation");
            throw new RuntimeException("Username and password are required");
        }
        if (userRepository.findByUsername(emp.getUsername()).isPresent()) {
            throw new UsernameAlreadyExistsException("Username already exists");
        }
        logger.debug("Encoding password for username: {}", emp.getUsername());

        User user = new User();
        user.setUsername(emp.getUsername());
        user.setPassword(passwordEncoder.encode(emp.getPassword()));
        user.setRole("USER");
        user.setEmail(emp.getEmail());
        user.setDepartment(emp.getDepartment());

        userRepository.save(user);

        logger.info("User account created successfully for username: {}", emp.getUsername());

        Employee employee = new Employee();
        employee.setName(emp.getName());
        employee.setEmail(emp.getEmail());
        employee.setDepartment(emp.getDepartment());
        employee.setUser(user);

        Employee saved = employeeRepo.save(employee);

        logger.info("Employee record saved successfully with id: {}", saved.getId());

        logger.trace("Employee full entity details: {}", saved);

        return modelMapper.map(saved, EmployeeDto.class);
    }

    /*---------------------------------------------GetById---------------------------------------------*/

    @Override
    public EmployeeDto getEmployeeById(Long id) {

        logger.trace("Fetching employee with id: {}", id);

        return employeeRepo.findById(id)
                .map(emp -> {
                    logger.trace("Employee found in database: {}", emp);
                    return modelMapper.map(emp, EmployeeDto.class);
                })
                .orElseThrow(() -> {
                    logger.error("Employee not found with id: {}", id);
                    return new EmployeeNotFoundException(
                            "Employee not found with id: " + id);
                });
    }

    /*---------------------------------------------GetByUsername---------------------------------------------*/

    @Override
    public UserDto getUserByUsername(String username) {

        User user = userRepository
                .findByUsername(username)
                .orElseThrow(() -> {
                    logger.error("User not found for username: {}", username);
                    return new RuntimeException("User not found");
                });

        return new UserDto(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getDepartment(),
                user.getRole(),
                user.isBlocked(),
                user.getBlockType()
        );
    }

    /*---------------------------------------------GetByEmail---------------------------------------------*/

    @Override
    public EmployeeDto getEmployeeByEmail(String email) {

        logger.info("Fetching employee details for email: {}", email);

        Employee employee = employeeRepo
                .findByEmail(email)
                .orElseThrow(() -> {
                    logger.error("Employee not found for email: {}", email);
                    return new RuntimeException("Employee not found");
                });

        logger.trace("Employee found with id: {}", employee.getId());

        return modelMapper.map(employee, EmployeeDto.class);
    }

    /*---------------------------------------------GetAllEmployee---------------------------------------------*/

    @Override
    public List<EmployeeDto> getAllEmployee() {

        logger.trace("Fetching all employees");

        List<Employee> employees = employeeRepo.findAll();

        if (employees.isEmpty()) {
            logger.warn("Employee list is empty");
        } else {
            logger.debug("Total employees found: {}", employees.size());
        }

        return employees.stream()
                .map(employee -> modelMapper.map(employee, EmployeeDto.class))
                .toList();
    }

    /*---------------------------------------------GetAllUsers---------------------------------------------*/

    @Override
    public List<UserDto> getAllUsers() {

        return userRepository.findAll()
                .stream()
                .map(user -> new UserDto(
                        user.getId(),
                        user.getUsername(),
                        user.getEmail(),
                        user.getDepartment(),
                        user.getRole(),
                        user.isBlocked(),
                        user.getBlockType()
                ))
                .toList();
    }

    /*---------------------------------------------UpdateById---------------------------------------------*/

    @Override
    public EmployeeDto updateEmployee(Long id, EmployeeDto employeeDto) {

        logger.info("Updating employee with id: {}", id);

        Employee existingEmployee = employeeRepo.findById(id)
                .orElseThrow(() -> {
                    logger.error("Employee not found while updating id: {}", id);
                    return new EmployeeNotFoundException(
                            "Employee not found with id: " + id);
                });

        logger.debug("Existing employee data before update: {}", existingEmployee);

        existingEmployee.setName(employeeDto.getName());
        existingEmployee.setEmail(employeeDto.getEmail());
        // update user also
        User user = existingEmployee.getUser();

        if (employeeDto.getUsername() != null) {
            user.setUsername(employeeDto.getUsername());
        }

        if (employeeDto.getEmail() != null) {
            user.setEmail(employeeDto.getEmail());
        }

        Employee updated = employeeRepo.save(existingEmployee);

        logger.info("Employee updated successfully with id: {}", id);

        return modelMapper.map(updated, EmployeeDto.class);
    }

    /*---------------------------------------------DeleteById---------------------------------------------*/

    @Override
    public void deleteEmployee(Long id) {

        logger.info("Deleting employee with id: {}", id);

        Employee emp = employeeRepo.findById(id)
                .orElseThrow(() -> {
                    logger.error("Employee not found while deleting id: {}", id);
                    return new EmployeeNotFoundException(
                            "Employee not found with id: " + id);
                });
        User user = emp.getUser();

        userRepository.delete(user);
        employeeRepo.deleteById(id);


        logger.warn("Employee deleted successfully with id: {}", id);
    }
}