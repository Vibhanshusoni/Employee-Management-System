package com.example.Employee.Service.ServiceImpl;

import com.example.Employee.Dto.DashboardDto;
import com.example.Employee.Repository.EmployeeRepository;
import com.example.Employee.Repository.UserRepository;
import com.example.Employee.Service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final EmployeeRepository employeeRepo;
    private final UserRepository userRepo;

    /*---------------------------------------------GetDashBoard---------------------------------------------*/

    public DashboardDto getDashboardData() {

        long totalEmployees = employeeRepo.count();
        long totalUsers = userRepo.count();
        long itCount = employeeRepo.countByDepartment("IT");
        long hrCount = employeeRepo.countByDepartment("HR");

        DashboardDto dashboard = new DashboardDto();
        dashboard.setTotalEmployees(totalEmployees);
        dashboard.setTotalUsers(totalUsers);
        dashboard.setItDepartmentCount(itCount);
        dashboard.setHrDepartmentCount(hrCount);

        return dashboard;
    }
}
