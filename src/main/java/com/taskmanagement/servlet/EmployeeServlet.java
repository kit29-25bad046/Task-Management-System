package com.taskmanagement.servlet;

import com.taskmanagement.model.Employee;
import com.taskmanagement.service.EmployeeManager;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

/**
 * Servlet handling Employee REST endpoints at /api/employees.
 */
@WebServlet(name = "EmployeeServlet", urlPatterns = {"/api/employees"})
public class EmployeeServlet extends BaseServlet {

    private final EmployeeManager employeeManager = new EmployeeManager();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String idParam = req.getParameter("id");
        String searchParam = req.getParameter("search");

        try {
            if (idParam != null && !idParam.trim().isEmpty()) {
                int empId = Integer.parseInt(idParam.trim());
                Employee emp = employeeManager.getEmployeeById(empId);
                if (emp != null) {
                    writeSuccess(resp, emp, "Employee retrieved successfully");
                } else {
                    writeError(resp, HttpServletResponse.SC_NOT_FOUND, "Employee with ID " + empId + " not found.");
                }
            } else if (searchParam != null && !searchParam.trim().isEmpty()) {
                List<Employee> list = employeeManager.searchEmployees(searchParam);
                writeSuccess(resp, list, "Employees matching query retrieved");
            } else {
                List<Employee> list = employeeManager.getAllEmployees();
                writeSuccess(resp, list, "All employees retrieved");
            }
        } catch (NumberFormatException e) {
            writeError(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid Employee ID format.");
        } catch (SQLException e) {
            writeError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database error: " + e.getMessage());
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            String body = readRequestBody(req);
            Employee emp = gson.fromJson(body, Employee.class);
            employeeManager.addEmployee(emp);
            writeSuccess(resp, emp, "Employee registered successfully");
        } catch (IllegalArgumentException e) {
            writeError(resp, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        } catch (SQLException e) {
            writeError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database error: " + e.getMessage());
        } catch (Exception e) {
            writeError(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid request payload: " + e.getMessage());
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            String body = readRequestBody(req);
            Employee emp = gson.fromJson(body, Employee.class);
            employeeManager.updateEmployee(emp);
            writeSuccess(resp, emp, "Employee updated successfully");
        } catch (IllegalArgumentException e) {
            writeError(resp, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        } catch (SQLException e) {
            writeError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database error: " + e.getMessage());
        } catch (Exception e) {
            writeError(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid request payload: " + e.getMessage());
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String idParam = req.getParameter("id");
        try {
            if (idParam == null || idParam.trim().isEmpty()) {
                writeError(resp, HttpServletResponse.SC_BAD_REQUEST, "Employee ID parameter is required for deletion.");
                return;
            }
            int empId = Integer.parseInt(idParam.trim());
            boolean deleted = employeeManager.deleteEmployee(empId);
            if (deleted) {
                writeSuccess(resp, null, "Employee ID " + empId + " deleted successfully.");
            } else {
                writeError(resp, HttpServletResponse.SC_NOT_FOUND, "Employee ID " + empId + " not found.");
            }
        } catch (NumberFormatException e) {
            writeError(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid Employee ID format.");
        } catch (SQLException e) {
            writeError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database error: " + e.getMessage());
        }
    }
}
