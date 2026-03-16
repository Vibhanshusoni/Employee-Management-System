package com.example.Employee.Repository;

import com.example.Employee.Entity.Employee;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
@Profile("jdbc")
public class EmployeeJdbcRepository implements EmployeeRepository {

    @Autowired
    private DataSource dataSource;

    @Override
    public Employee save(Employee emp) {

        String sql = "INSERT INTO employees (name,email,department) VALUES (?,?,?)";

        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, emp.getName());
            ps.setString(2, emp.getEmail());
            ps.setString(3, emp.getDepartment());

            ps.executeUpdate();

        } catch (Exception e) {
            throw new RuntimeException("Database error while saving employee", e);
        }

        return emp;
    }

    @Override
    public List<Employee> findAll() {

        List<Employee> list = new ArrayList<>();
        String sql = "SELECT * FROM employees";

        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(mapEmployee(rs));
            }

        } catch (Exception e) {
            throw new RuntimeException("Database error while fetching employees", e);
        }

        return list;
    }

    @Override
    public Optional<Employee> findById(Long id) {

        String sql = "SELECT * FROM employees WHERE id=?";

        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setLong(1, id);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return Optional.of(mapEmployee(rs));
            }

        } catch (Exception e) {
            throw new RuntimeException("Database error while fetching employee by id", e);
        }

        return Optional.empty();
    }

    @Override
    public Optional<Employee> findByEmail(String email) {

        String sql = "SELECT * FROM employees WHERE email=?";

        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, email);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return Optional.of(mapEmployee(rs));
            }

        } catch (Exception e) {
            throw new RuntimeException("Database error while fetching employee by email", e);
        }

        return Optional.empty();
    }

    @Override
    public Optional<Employee> findByUserUsername(String username) {

        String sql = """
                SELECT e.*
                FROM employees e
                JOIN users u ON e.user_id = u.id
                WHERE u.username = ?
                """;

        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, username);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return Optional.of(mapEmployee(rs));
            }

        } catch (Exception e) {
            throw new RuntimeException("Database error while fetching employee by username", e);
        }

        return Optional.empty();
    }

    @Override
    public long countByDepartment(String department) {

        String sql = "SELECT COUNT(*) FROM employees WHERE department = ?";

        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, department);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getLong(1);
            }

        } catch (Exception e) {
            throw new RuntimeException("Database error while counting employees by department", e);
        }

        return 0;
    }

    @Override
    public long count() {

        String sql = "SELECT COUNT(*) FROM employees";

        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                return rs.getLong(1);
            }

        } catch (Exception e) {
            throw new RuntimeException("Database error while counting employees", e);
        }

        return 0;
    }

    @Override
    public void deleteById(Long id) {

        String sql = "DELETE FROM employees WHERE id=?";

        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setLong(1, id);
            ps.executeUpdate();

        } catch (Exception e) {
            throw new RuntimeException("Database error while deleting employee", e);
        }
    }

    private Employee mapEmployee(ResultSet rs) throws Exception {

        Employee emp = new Employee();

        emp.setId(rs.getLong("id"));
        emp.setName(rs.getString("name"));
        emp.setEmail(rs.getString("email"));
        emp.setDepartment(rs.getString("department"));

        return emp;
    }
}

