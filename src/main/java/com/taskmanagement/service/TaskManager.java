package com.taskmanagement.service;

import com.taskmanagement.dao.EmployeeDAO;
import com.taskmanagement.dao.TaskDAO;
import com.taskmanagement.model.Task;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.*;

/**
 * Service class for Task operations.
 * Demonstrates the explicit use of Java Collections:
 * 1. ArrayList<Task> - Store, iterate, and process tasks retrieved from MySQL.
 * 2. HashMap<Integer, ArrayList<Task>> - Map each Employee ID to their assigned task list.
 * 3. PriorityQueue<Task> - Efficiently prioritize tasks by priority (HIGH -> MEDIUM -> LOW)
 *    and earlier deadlines.
 */
public class TaskManager {

    private final TaskDAO taskDAO;
    private final EmployeeDAO employeeDAO;

    public TaskManager() {
        this.taskDAO = new TaskDAO();
        this.employeeDAO = new EmployeeDAO();
    }

    public TaskManager(TaskDAO taskDAO, EmployeeDAO employeeDAO) {
        this.taskDAO = taskDAO;
        this.employeeDAO = employeeDAO;
    }

    /**
     * Demonstrates ArrayList<Task>:
     * Retrieves all tasks from MySQL using JDBC and stores them in an ArrayList.
     */
    public ArrayList<Task> getAllTasks() throws SQLException {
        List<Task> rawList = taskDAO.getAllTasks();
        return new ArrayList<>(rawList);
    }

    /**
     * Demonstrates HashMap<Integer, ArrayList<Task>>:
     * Maps each Employee ID to their corresponding list of assigned tasks.
     * Example: Employee 101 -> [Task 1001, Task 1005]
     */
    public HashMap<Integer, ArrayList<Task>> getEmployeeTaskMap() throws SQLException {
        ArrayList<Task> allTasks = getAllTasks();
        HashMap<Integer, ArrayList<Task>> taskMap = new HashMap<>();

        for (Task task : allTasks) {
            int empId = task.getEmployeeId();
            taskMap.computeIfAbsent(empId, k -> new ArrayList<>()).add(task);
        }
        return taskMap;
    }

    /**
     * Retrieves tasks for a specific employee using the HashMap.
     */
    public ArrayList<Task> getTasksForEmployee(int employeeId) throws SQLException {
        HashMap<Integer, ArrayList<Task>> map = getEmployeeTaskMap();
        return map.getOrDefault(employeeId, new ArrayList<>());
    }

    /**
     * Demonstrates PriorityQueue<Task>:
     * Organizes tasks based on Priority (HIGH -> MEDIUM -> LOW) and earlier deadline.
     * Natural ordering is governed by Task.compareTo().
     */
    public PriorityQueue<Task> getPriorityTaskQueue() throws SQLException {
        ArrayList<Task> allTasks = getAllTasks();
        PriorityQueue<Task> queue = new PriorityQueue<>();

        // Add pending and in-progress tasks to priority queue (exclude completed)
        for (Task task : allTasks) {
            if (!Task.STATUS_COMPLETED.equalsIgnoreCase(task.getStatus())) {
                queue.offer(task);
            }
        }
        return queue;
    }

    /**
     * Polls the PriorityQueue into an ordered ArrayList<Task> for frontend display.
     */
    public ArrayList<Task> getSortedPriorityTasks() throws SQLException {
        PriorityQueue<Task> queue = getPriorityTaskQueue();
        ArrayList<Task> sortedList = new ArrayList<>();
        while (!queue.isEmpty()) {
            sortedList.add(queue.poll());
        }
        return sortedList;
    }

    /**
     * Computes dashboard KPIs across tasks.
     */
    public Map<String, Object> getDashboardStatistics() throws SQLException {
        ArrayList<Task> tasks = getAllTasks();
        int totalEmployees = employeeDAO.getAllEmployees().size();

        int totalTasks = tasks.size();
        int pendingTasks = 0;
        int inProgressTasks = 0;
        int completedTasks = 0;
        int overdueTasks = 0;
        int dueSoonTasks = 0;

        LocalDate today = LocalDate.now();

        for (Task t : tasks) {
            String status = t.getStatus();
            if (Task.STATUS_COMPLETED.equalsIgnoreCase(status)) {
                completedTasks++;
            } else {
                if (Task.STATUS_PENDING.equalsIgnoreCase(status)) {
                    pendingTasks++;
                } else if (Task.STATUS_IN_PROGRESS.equalsIgnoreCase(status)) {
                    inProgressTasks++;
                }

                if (t.getDeadline() != null) {
                    if (today.isAfter(t.getDeadline())) {
                        overdueTasks++;
                    } else {
                        long daysUntil = java.time.temporal.ChronoUnit.DAYS.between(today, t.getDeadline());
                        if (daysUntil <= 2) {
                            dueSoonTasks++;
                        }
                    }
                }
            }
        }

        double completionRate = totalTasks > 0 ? ((double) completedTasks / totalTasks) * 100 : 0.0;

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalEmployees", totalEmployees);
        stats.put("totalTasks", totalTasks);
        stats.put("pendingTasks", pendingTasks);
        stats.put("inProgressTasks", inProgressTasks);
        stats.put("completedTasks", completedTasks);
        stats.put("overdueTasks", overdueTasks);
        stats.put("dueSoonTasks", dueSoonTasks);
        stats.put("completionRate", Math.round(completionRate * 10.0) / 10.0);

        return stats;
    }

    /**
     * Validates and creates a new task.
     */
    public void addTask(Task task) throws IllegalArgumentException, SQLException {
        validateTask(task, true);
        boolean success = taskDAO.insertTask(task);
        if (!success) {
            throw new SQLException("Failed to insert task into database.");
        }
    }

    /**
     * Validates and updates a task.
     */
    public void updateTask(Task task) throws IllegalArgumentException, SQLException {
        validateTask(task, false);
        if (!taskDAO.isTaskExists(task.getTaskId())) {
            throw new IllegalArgumentException("Task ID " + task.getTaskId() + " does not exist.");
        }
        boolean success = taskDAO.updateTask(task);
        if (!success) {
            throw new SQLException("Failed to update task.");
        }
    }

    /**
     * Updates task status and manages completion date.
     */
    public boolean updateTaskStatus(int taskId, String status) throws SQLException, IllegalArgumentException {
        if (status == null || (!Task.STATUS_PENDING.equalsIgnoreCase(status)
                && !Task.STATUS_IN_PROGRESS.equalsIgnoreCase(status)
                && !Task.STATUS_COMPLETED.equalsIgnoreCase(status))) {
            throw new IllegalArgumentException("Status must be PENDING, IN PROGRESS, or COMPLETED.");
        }
        String normalizedStatus = status.trim().toUpperCase();
        LocalDate completionDate = Task.STATUS_COMPLETED.equals(normalizedStatus) ? LocalDate.now() : null;
        return taskDAO.updateTaskStatus(taskId, normalizedStatus, completionDate);
    }

    public boolean deleteTask(int taskId) throws SQLException {
        return taskDAO.deleteTask(taskId);
    }

    public Task getTaskById(int taskId) throws SQLException {
        return taskDAO.getTaskById(taskId);
    }

    public ArrayList<Task> searchTasks(String query) throws SQLException {
        return new ArrayList<>(taskDAO.searchTasks(query));
    }

    public ArrayList<Task> filterTasks(String priority, String status, Integer employeeId) throws SQLException {
        return new ArrayList<>(taskDAO.filterTasks(priority, status, employeeId));
    }

    private void validateTask(Task task, boolean isNew) throws IllegalArgumentException, SQLException {
        if (task == null) {
            throw new IllegalArgumentException("Task object cannot be null.");
        }
        if (task.getTaskId() <= 0) {
            throw new IllegalArgumentException("Task ID must be a positive integer.");
        }
        if (isNew && taskDAO.isTaskExists(task.getTaskId())) {
            throw new IllegalArgumentException("Task ID " + task.getTaskId() + " already exists. Must be unique.");
        }
        if (task.getTaskTitle() == null || task.getTaskTitle().trim().isEmpty()) {
            throw new IllegalArgumentException("Task title is required.");
        }
        if (task.getEmployeeId() <= 0) {
            throw new IllegalArgumentException("A valid Employee ID is required.");
        }
        if (!employeeDAO.isEmployeeExists(task.getEmployeeId())) {
            throw new IllegalArgumentException("Assigned employee ID " + task.getEmployeeId() + " does not exist.");
        }
        String prio = task.getPriority();
        if (prio == null || (!Task.PRIORITY_HIGH.equalsIgnoreCase(prio)
                && !Task.PRIORITY_MEDIUM.equalsIgnoreCase(prio)
                && !Task.PRIORITY_LOW.equalsIgnoreCase(prio))) {
            throw new IllegalArgumentException("Priority must be HIGH, MEDIUM, or LOW.");
        }
        String stat = task.getStatus();
        if (stat == null || (!Task.STATUS_PENDING.equalsIgnoreCase(stat)
                && !Task.STATUS_IN_PROGRESS.equalsIgnoreCase(stat)
                && !Task.STATUS_COMPLETED.equalsIgnoreCase(stat))) {
            throw new IllegalArgumentException("Status must be PENDING, IN PROGRESS, or COMPLETED.");
        }
        if (task.getDeadline() == null) {
            throw new IllegalArgumentException("Valid deadline date is required.");
        }
        if (Task.STATUS_COMPLETED.equalsIgnoreCase(stat) && task.getCompletionDate() == null) {
            task.setCompletionDate(LocalDate.now());
        }
    }
}
