package com.taskmanagement.servlet;

import com.taskmanagement.config.DBConnection;
import com.taskmanagement.service.TaskManager;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Map;

/**
 * Servlet handling Dashboard statistical metrics at /api/dashboard.
 */
@WebServlet(name = "DashboardServlet", urlPatterns = {"/api/dashboard"})
public class DashboardServlet extends BaseServlet {

    private final TaskManager taskManager = new TaskManager();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            Map<String, Object> stats = taskManager.getDashboardStatistics();
            stats.put("databaseConnected", true);
            stats.put("databaseUrl", DBConnection.getDbUrl());
            writeSuccess(resp, stats, "Dashboard statistics retrieved successfully");
        } catch (SQLException e) {
            // Return degraded state with helpful connection message
            Map<String, Object> fallback = Map.of(
                    "databaseConnected", false,
                    "databaseError", e.getMessage(),
                    "totalEmployees", 0,
                    "totalTasks", 0,
                    "pendingTasks", 0,
                    "inProgressTasks", 0,
                    "completedTasks", 0,
                    "overdueTasks", 0,
                    "dueSoonTasks", 0,
                    "completionRate", 0.0
            );
            writeSuccess(resp, fallback, "Database connection not established. Please check database.properties.");
        }
    }
}
