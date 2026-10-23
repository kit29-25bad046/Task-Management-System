package com.taskmanagement.servlet;

import com.google.gson.JsonObject;
import com.taskmanagement.model.Task;
import com.taskmanagement.service.TaskManager;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Servlet handling Task REST endpoints at /api/tasks.
 * Demonstrates ArrayList, HashMap, and PriorityQueue data delivery.
 */
@WebServlet(name = "TaskServlet", urlPatterns = {"/api/tasks"})
public class TaskServlet extends BaseServlet {

    private final TaskManager taskManager = new TaskManager();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String idParam = req.getParameter("id");
        String typeParam = req.getParameter("type");
        String empIdParam = req.getParameter("employeeId");
        String searchParam = req.getParameter("search");
        String priorityParam = req.getParameter("priority");
        String statusParam = req.getParameter("status");

        try {
            // 1. Single Task by ID
            if (idParam != null && !idParam.trim().isEmpty()) {
                int taskId = Integer.parseInt(idParam.trim());
                Task task = taskManager.getTaskById(taskId);
                if (task != null) {
                    writeSuccess(resp, task, "Task retrieved successfully");
                } else {
                    writeError(resp, HttpServletResponse.SC_NOT_FOUND, "Task ID " + taskId + " not found.");
                }
                return;
            }

            // 2. Priority Tasks (Demonstrating PriorityQueue)
            if ("priority".equalsIgnoreCase(typeParam)) {
                ArrayList<Task> priorityList = taskManager.getSortedPriorityTasks();
                writeSuccess(resp, priorityList, "Priority tasks retrieved via PriorityQueue (HIGH -> MEDIUM -> LOW, earlier deadline first)");
                return;
            }

            // 3. Employee Tasks Map (Demonstrating HashMap<Integer, ArrayList<Task>>)
            if ("employee-map".equalsIgnoreCase(typeParam)) {
                HashMap<Integer, ArrayList<Task>> map = taskManager.getEmployeeTaskMap();
                writeSuccess(resp, map, "Tasks grouped by employee ID retrieved via HashMap");
                return;
            }

            // 4. Tasks for specific employee
            if (empIdParam != null && !empIdParam.trim().isEmpty()) {
                int empId = Integer.parseInt(empIdParam.trim());
                ArrayList<Task> empTasks = taskManager.getTasksForEmployee(empId);
                writeSuccess(resp, empTasks, "Tasks for employee ID " + empId + " retrieved");
                return;
            }

            // 5. Search tasks by keyword
            if (searchParam != null && !searchParam.trim().isEmpty()) {
                ArrayList<Task> results = taskManager.searchTasks(searchParam);
                writeSuccess(resp, results, "Tasks matching search query retrieved");
                return;
            }

            // 6. Filter tasks by priority and/or status
            if ((priorityParam != null && !priorityParam.trim().isEmpty()) ||
                (statusParam != null && !statusParam.trim().isEmpty())) {
                ArrayList<Task> filtered = taskManager.filterTasks(priorityParam, statusParam, null);
                writeSuccess(resp, filtered, "Filtered tasks retrieved");
                return;
            }

            // 7. Default: All tasks as ArrayList<Task>
            ArrayList<Task> allTasks = taskManager.getAllTasks();
            writeSuccess(resp, allTasks, "All tasks retrieved into ArrayList");

        } catch (NumberFormatException e) {
            writeError(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid numeric ID parameter.");
        } catch (SQLException e) {
            writeError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database error: " + e.getMessage());
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            String body = readRequestBody(req);
            Task task = gson.fromJson(body, Task.class);
            taskManager.addTask(task);
            writeSuccess(resp, task, "Task created and assigned successfully");
        } catch (IllegalArgumentException e) {
            writeError(resp, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        } catch (SQLException e) {
            writeError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database error: " + e.getMessage());
        } catch (Exception e) {
            writeError(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid task payload: " + e.getMessage());
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            String body = readRequestBody(req);
            JsonObject json = gson.fromJson(body, JsonObject.class);

            // Check if this is a quick status update
            if (json.has("action") && "status".equalsIgnoreCase(json.get("action").getAsString())) {
                int taskId = json.get("taskId").getAsInt();
                String status = json.get("status").getAsString();
                boolean updated = taskManager.updateTaskStatus(taskId, status);
                if (updated) {
                    writeSuccess(resp, null, "Task status updated to " + status);
                } else {
                    writeError(resp, HttpServletResponse.SC_NOT_FOUND, "Task ID " + taskId + " not found.");
                }
                return;
            }

            // Full task update
            Task task = gson.fromJson(json, Task.class);
            taskManager.updateTask(task);
            writeSuccess(resp, task, "Task updated successfully");

        } catch (IllegalArgumentException e) {
            writeError(resp, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        } catch (SQLException e) {
            writeError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database error: " + e.getMessage());
        } catch (Exception e) {
            writeError(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid task payload: " + e.getMessage());
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String idParam = req.getParameter("id");
        try {
            if (idParam == null || idParam.trim().isEmpty()) {
                writeError(resp, HttpServletResponse.SC_BAD_REQUEST, "Task ID parameter is required for deletion.");
                return;
            }
            int taskId = Integer.parseInt(idParam.trim());
            boolean deleted = taskManager.deleteTask(taskId);
            if (deleted) {
                writeSuccess(resp, null, "Task ID " + taskId + " deleted successfully.");
            } else {
                writeError(resp, HttpServletResponse.SC_NOT_FOUND, "Task ID " + taskId + " not found.");
            }
        } catch (NumberFormatException e) {
            writeError(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid Task ID format.");
        } catch (SQLException e) {
            writeError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database error: " + e.getMessage());
        }
    }
}
