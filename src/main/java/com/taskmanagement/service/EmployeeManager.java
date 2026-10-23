package com.taskmanagement.service;

import com.taskmanagement.dao.EmployeeDAO;
import com.taskmanagement.model.Employee;

import java.sql.SQLException;
import java.util.List;

/**
 * Service/Manager class handling Employee business rules and validation.
 */
public class EmployeeManager {

    private final EmployeeDAO employeeDAO;

    public EmployeeManager() {
        this.employeeDAO = new EmployeeDAO();
    }

    public EmployeeManager(EmployeeDAO employeeDAO) {
        this.employeeDAO = employeeDAO;
    }

    /**
     * Validates and registers a new employee.
     */
    public void addEmployee(Employee emp) throws IllegalArgumentException, SQLException {
        validateEmployee(emp, true);
        boolean success = employeeDAO.insertEmployee(emp);
        if (!success) {
            throw new SQLException("Failed to insert employee into database.");
        }
    }

    /**
     * Validates and updates an existing employee.
     */
    public void updateEmployee(Employee emp) throws IllegalArgumentException, SQLException {
        validateEmployee(emp, false);
        if (!employeeDAO.isEmployeeExists(emp.getEmployeeId())) {
            throw new IllegalArgumentException("Employee ID " + emp.getEmployeeId() + " does not exist.");
        }
        boolean success = employeeDAO.updateEmployee(emp);
        if (!success) {
            throw new SQLException("Failed to update employee.");
        }
    }

    public boolean deleteEmployee(int employeeId) throws SQLException {
        return employeeDAO.deleteEmployee(employeeId);
    }

    public Employee getEmployeeById(int employeeId) throws SQLException {
        return employeeDAO.getEmployeeById(employeeId);
    }

    public List<Employee> getAllEmployees() throws SQLException {
        return employeeDAO.getAllEmployees();
    }

    public List<Employee> searchEmployees(String query) throws SQLException {
        return employeeDAO.searchEmployees(query);
    }

    public boolean isEmployeeExists(int employeeId) throws SQLException {
        return employeeDAO.isEmployeeExists(employeeId);
    }

    private void validateEmployee(Employee emp, boolean isNew) throws IllegalArgumentException, SQLException {
        if (emp == null) {
            throw new IllegalArgumentException("Employee object cannot be null.");
        }
        if (emp.getEmployeeId() <= 0) {
            throw new IllegalArgumentException("Employee ID must be a positive integer.");
        }
        if (isNew && employeeDAO.isEmployeeExists(emp.getEmployeeId())) {
            throw new IllegalArgumentException("Employee ID " + emp.getEmployeeId() + " already exists. Must be unique.");
        }
        if (emp.getEmployeeName() == null || emp.getEmployeeName().trim().isEmpty()) {
            throw new IllegalArgumentException("Employee name is required and cannot be empty.");
        }
        if (emp.getEmail() == null || emp.getEmail().trim().isEmpty()) {
            throw new IllegalArgumentException("Employee email is required and cannot be empty.");
        }
        if (!emp.getEmail().matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            throw new IllegalArgumentException("Invalid email format.");
        }
        if (emp.getDepartment() == null || emp.getDepartment().trim().isEmpty()) {
            throw new IllegalArgumentException("Employee department is required and cannot be empty.");
        }
    }
}
