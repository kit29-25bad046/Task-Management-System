package com.taskmanagement.dao;

import com.taskmanagement.config.DBConnection;
import com.taskmanagement.model.Employee;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Employee operations using JDBC PreparedStatements.
 */
public class EmployeeDAO {

    /**
     * Inserts a new employee into MySQL.
     */
    public boolean insertEmployee(Employee emp) throws SQLException {
        String sql = "INSERT INTO employees (employee_id, employee_name, email, department) VALUES (?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, emp.getEmployeeId());
            pstmt.setString(2, emp.getEmployeeName());
            pstmt.setString(3, emp.getEmail());
            pstmt.setString(4, emp.getDepartment());
            return pstmt.executeUpdate() > 0;
        }
    }

    /**
     * Retrieves all employees from MySQL.
     */
    public List<Employee> getAllEmployees() throws SQLException {
        List<Employee> list = new ArrayList<>();
        String sql = "SELECT employee_id, employee_name, email, department FROM employees ORDER BY employee_id ASC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                list.add(extractEmployee(rs));
            }
        }
        return list;
    }

    /**
     * Finds employee by ID.
     */
    public Employee getEmployeeById(int employeeId) throws SQLException {
        String sql = "SELECT employee_id, employee_name, email, department FROM employees WHERE employee_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, employeeId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return extractEmployee(rs);
                }
            }
        }
        return null;
    }

    /**
     * Checks if an employee ID already exists.
     */
    public boolean isEmployeeExists(int employeeId) throws SQLException {
        String sql = "SELECT 1 FROM employees WHERE employee_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, employeeId);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }
        }
    }

    /**
     * Updates an existing employee.
     */
    public boolean updateEmployee(Employee emp) throws SQLException {
        String sql = "UPDATE employees SET employee_name = ?, email = ?, department = ? WHERE employee_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, emp.getEmployeeName());
            pstmt.setString(2, emp.getEmail());
            pstmt.setString(3, emp.getDepartment());
            pstmt.setInt(4, emp.getEmployeeId());
            return pstmt.executeUpdate() > 0;
        }
    }

    /**
     * Deletes an employee by ID (cascades to their tasks).
     */
    public boolean deleteEmployee(int employeeId) throws SQLException {
        String sql = "DELETE FROM employees WHERE employee_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, employeeId);
            return pstmt.executeUpdate() > 0;
        }
    }

    /**
     * Searches employees by keyword across ID, name, email, and department.
     */
    public List<Employee> searchEmployees(String query) throws SQLException {
        List<Employee> list = new ArrayList<>();
        String sql = "SELECT employee_id, employee_name, email, department FROM employees " +
                     "WHERE CAST(employee_id AS CHAR) LIKE ? OR LOWER(employee_name) LIKE ? " +
                     "OR LOWER(email) LIKE ? OR LOWER(department) LIKE ? ORDER BY employee_id ASC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            String pattern = "%" + (query != null ? query.trim().toLowerCase() : "") + "%";
            pstmt.setString(1, pattern);
            pstmt.setString(2, pattern);
            pstmt.setString(3, pattern);
            pstmt.setString(4, pattern);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(extractEmployee(rs));
                }
            }
        }
        return list;
    }

    private Employee extractEmployee(ResultSet rs) throws SQLException {
        return new Employee(
                rs.getInt("employee_id"),
                rs.getString("employee_name"),
                rs.getString("email"),
                rs.getString("department")
        );
    }
}
