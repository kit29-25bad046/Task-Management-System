package com.taskmanagement.dao;

import com.taskmanagement.config.DBConnection;
import com.taskmanagement.model.Task;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Task operations using JDBC PreparedStatements.
 */
public class TaskDAO {

    /**
     * Inserts a new task into MySQL.
     */
    public boolean insertTask(Task task) throws SQLException {
        String sql = "INSERT INTO tasks (task_id, task_title, description, employee_id, priority, deadline, status, completion_date) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, task.getTaskId());
            pstmt.setString(2, task.getTaskTitle());
            pstmt.setString(3, task.getDescription());
            pstmt.setInt(4, task.getEmployeeId());
            pstmt.setString(5, task.getPriority());
            pstmt.setDate(6, task.getDeadline() != null ? Date.valueOf(task.getDeadline()) : null);
            pstmt.setString(7, task.getStatus());
            pstmt.setDate(8, task.getCompletionDate() != null ? Date.valueOf(task.getCompletionDate()) : null);
            return pstmt.executeUpdate() > 0;
        }
    }

    /**
     * Retrieves all tasks with joined employee details.
     */
    public List<Task> getAllTasks() throws SQLException {
        List<Task> list = new ArrayList<>();
        String sql = "SELECT t.task_id, t.task_title, t.description, t.employee_id, e.employee_name, " +
                     "t.priority, t.deadline, t.status, t.completion_date " +
                     "FROM tasks t LEFT JOIN employees e ON t.employee_id = e.employee_id " +
                     "ORDER BY t.task_id ASC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                list.add(extractTask(rs));
            }
        }
        return list;
    }

    /**
     * Finds task by ID.
     */
    public Task getTaskById(int taskId) throws SQLException {
        String sql = "SELECT t.task_id, t.task_title, t.description, t.employee_id, e.employee_name, " +
                     "t.priority, t.deadline, t.status, t.completion_date " +
                     "FROM tasks t LEFT JOIN employees e ON t.employee_id = e.employee_id " +
                     "WHERE t.task_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, taskId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return extractTask(rs);
                }
            }
        }
        return null;
    }

    /**
     * Retrieves all tasks for a specific employee ID.
     */
    public List<Task> getTasksByEmployeeId(int employeeId) throws SQLException {
        List<Task> list = new ArrayList<>();
        String sql = "SELECT t.task_id, t.task_title, t.description, t.employee_id, e.employee_name, " +
                     "t.priority, t.deadline, t.status, t.completion_date " +
                     "FROM tasks t LEFT JOIN employees e ON t.employee_id = e.employee_id " +
                     "WHERE t.employee_id = ? ORDER BY t.deadline ASC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, employeeId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(extractTask(rs));
                }
            }
        }
        return list;
    }

    /**
     * Checks if task ID already exists.
     */
    public boolean isTaskExists(int taskId) throws SQLException {
        String sql = "SELECT 1 FROM tasks WHERE task_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, taskId);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }
        }
    }

    /**
     * Updates status and optionally completion date.
     */
    public boolean updateTaskStatus(int taskId, String status, LocalDate completionDate) throws SQLException {
        String sql = "UPDATE tasks SET status = ?, completion_date = ? WHERE task_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, status);
            pstmt.setDate(2, completionDate != null ? Date.valueOf(completionDate) : null);
            pstmt.setInt(3, taskId);
            return pstmt.executeUpdate() > 0;
        }
    }

    /**
     * Updates all editable task details.
     */
    public boolean updateTask(Task task) throws SQLException {
        String sql = "UPDATE tasks SET task_title = ?, description = ?, employee_id = ?, priority = ?, " +
                     "deadline = ?, status = ?, completion_date = ? WHERE task_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, task.getTaskTitle());
            pstmt.setString(2, task.getDescription());
            pstmt.setInt(3, task.getEmployeeId());
            pstmt.setString(4, task.getPriority());
            pstmt.setDate(5, task.getDeadline() != null ? Date.valueOf(task.getDeadline()) : null);
            pstmt.setString(6, task.getStatus());
            pstmt.setDate(7, task.getCompletionDate() != null ? Date.valueOf(task.getCompletionDate()) : null);
            pstmt.setInt(8, task.getTaskId());
            return pstmt.executeUpdate() > 0;
        }
    }

    /**
     * Deletes a task by ID.
     */
    public boolean deleteTask(int taskId) throws SQLException {
        String sql = "DELETE FROM tasks WHERE task_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, taskId);
            return pstmt.executeUpdate() > 0;
        }
    }

    /**
     * Searches tasks by keyword across title, description, and assignee name.
     */
    public List<Task> searchTasks(String query) throws SQLException {
        List<Task> list = new ArrayList<>();
        String sql = "SELECT t.task_id, t.task_title, t.description, t.employee_id, e.employee_name, " +
                     "t.priority, t.deadline, t.status, t.completion_date " +
                     "FROM tasks t LEFT JOIN employees e ON t.employee_id = e.employee_id " +
                     "WHERE CAST(t.task_id AS CHAR) LIKE ? OR LOWER(t.task_title) LIKE ? " +
                     "OR LOWER(t.description) LIKE ? OR LOWER(e.employee_name) LIKE ? " +
                     "ORDER BY t.task_id ASC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            String pattern = "%" + (query != null ? query.trim().toLowerCase() : "") + "%";
            pstmt.setString(1, pattern);
            pstmt.setString(2, pattern);
            pstmt.setString(3, pattern);
            pstmt.setString(4, pattern);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(extractTask(rs));
                }
            }
        }
        return list;
    }

    /**
     * Filters tasks by priority and/or status and/or employeeId.
     */
    public List<Task> filterTasks(String priority, String status, Integer employeeId) throws SQLException {
        StringBuilder sql = new StringBuilder(
                "SELECT t.task_id, t.task_title, t.description, t.employee_id, e.employee_name, " +
                "t.priority, t.deadline, t.status, t.completion_date " +
                "FROM tasks t LEFT JOIN employees e ON t.employee_id = e.employee_id WHERE 1=1"
        );
        List<Object> params = new ArrayList<>();
        if (priority != null && !priority.trim().isEmpty() && !"ALL".equalsIgnoreCase(priority)) {
            sql.append(" AND t.priority = ?");
            params.add(priority.trim().toUpperCase());
        }
        if (status != null && !status.trim().isEmpty() && !"ALL".equalsIgnoreCase(status)) {
            sql.append(" AND t.status = ?");
            params.add(status.trim().toUpperCase());
        }
        if (employeeId != null && employeeId > 0) {
            sql.append(" AND t.employee_id = ?");
            params.add(employeeId);
        }
        sql.append(" ORDER BY t.task_id ASC");

        List<Task> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                pstmt.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(extractTask(rs));
                }
            }
        }
        return list;
    }

    private Task extractTask(ResultSet rs) throws SQLException {
        Date dLine = rs.getDate("deadline");
        Date cDate = rs.getDate("completion_date");
        Task task = new Task(
                rs.getInt("task_id"),
                rs.getString("task_title"),
                rs.getString("description"),
                rs.getInt("employee_id"),
                rs.getString("priority"),
                dLine != null ? dLine.toLocalDate() : null,
                rs.getString("status"),
                cDate != null ? cDate.toLocalDate() : null
        );
        task.setEmployeeName(rs.getString("employee_name"));
        return task;
    }
}
