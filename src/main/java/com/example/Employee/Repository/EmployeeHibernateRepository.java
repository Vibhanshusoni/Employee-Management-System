package com.example.Employee.Repository;

import java.util.List;
import java.util.Optional;

import com.example.Employee.Entity.Employee;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;


@Repository
@Profile("hibernate")
@Transactional
public class EmployeeHibernateRepository implements EmployeeRepository {

    @PersistenceContext
    private EntityManager entityManager;
    private SessionFactory sessionFactory;

    @Override
    public Employee save(Employee emp) {
        if (emp.getId() == null) {
            entityManager.persist(emp);
            return emp;
        }
        return entityManager.merge(emp);
    }

    @Override
    public List<Employee> findAll() {
        return entityManager
                .createQuery("FROM Employee", Employee.class)
                .getResultList();
    }

    @Override
    public Optional<Employee> findById(Long id) {
        return Optional.ofNullable(
                entityManager.find(Employee.class, id));
    }

    @Override
    public Optional<Employee> findByEmail(String email) {
        return entityManager.createQuery(
                        "FROM Employee e WHERE e.email = :email",
                        Employee.class)
                .setParameter("email", email)
                .getResultStream()
                .findFirst();
    }

    @Override
    public Optional<Employee> findByUserUsername(String username) {

        try (Session session = sessionFactory.openSession()) {

            String hql = "FROM Employee e WHERE e.user.username = :username";

            Query<Employee> query = session.createQuery(hql, Employee.class);
            query.setParameter("username", username);

            Employee employee = query.uniqueResult();

            return Optional.ofNullable(employee);
        }
    }

    @Override
    public long countByDepartment(String department) {
        return 0;
    }

    @Override
    public void deleteById(Long id) {
        findById(id).ifPresent(entityManager::remove);
    }

    @Override
    public long count() {
        return 0;
    }
}
