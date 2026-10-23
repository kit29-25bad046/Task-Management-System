package com.taskmanagement.model;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

/**
 * Model class representing a Task in the system.
 * Implements Comparable for PriorityQueue ordering:
 * 1. Priority: HIGH -> MEDIUM -> LOW
 * 2. Earlier deadline comes first
 */
public class Task implements Serializable, Comparable<Task> {
    private static final long serialVersionUID = 1L;

    public static final String PRIORITY_HIGH = "HIGH";
    public static final String PRIORITY_MEDIUM = "MEDIUM";
    public static final String PRIORITY_LOW = "LOW";

    public static final String STATUS_PENDING = "PENDING";
    public static final String STATUS_IN_PROGRESS = "IN PROGRESS";
    public static final String STATUS_COMPLETED = "COMPLETED";

    private int taskId;
    private String taskTitle;
    private String description;
    private int employeeId;
    private String employeeName;
    private String priority;
    private LocalDate deadline;
    private String status;
    private LocalDate completionDate;

    public Task() {
    }

    public Task(int taskId, String taskTitle, String description, int employeeId,
                String priority, LocalDate deadline, String status, LocalDate completionDate) {
        this.taskId = taskId;
        this.taskTitle = taskTitle;
        this.description = description;
        this.employeeId = employeeId;
        this.priority = priority != null ? priority.trim().toUpperCase() : PRIORITY_MEDIUM;
        this.deadline = deadline;
        this.status = status != null ? status.trim().toUpperCase() : STATUS_PENDING;
        this.completionDate = completionDate;
    }

    public int getTaskId() {
        return taskId;
    }

    public void setTaskId(int taskId) {
        this.taskId = taskId;
    }

    public String getTaskTitle() {
        return taskTitle;
    }

    public void setTaskTitle(String taskTitle) {
        this.taskTitle = taskTitle;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(int employeeId) {
        this.employeeId = employeeId;
    }

    public String getEmployeeName() {
        return employeeName;
    }

    public void setEmployeeName(String employeeName) {
        this.employeeName = employeeName;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority != null ? priority.trim().toUpperCase() : PRIORITY_MEDIUM;
    }

    public LocalDate getDeadline() {
        return deadline;
    }

    public void setDeadline(LocalDate deadline) {
        this.deadline = deadline;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status != null ? status.trim().toUpperCase() : STATUS_PENDING;
    }

    public LocalDate getCompletionDate() {
        return completionDate;
    }

    public void setCompletionDate(LocalDate completionDate) {
        this.completionDate = completionDate;
    }

    /**
     * Calculates deadline status based on today's date:
     * - COMPLETED if task status is COMPLETED
     * - OVERDUE if today is after deadline
     * - DUE SOON if deadline is within 2 days (today, tomorrow, or in 2 days)
     * - ON TRACK otherwise
     */
    public String getDeadlineStatus() {
        if (STATUS_COMPLETED.equalsIgnoreCase(this.status)) {
            return "COMPLETED";
        }
        if (this.deadline == null) {
            return "ON TRACK";
        }
        LocalDate today = LocalDate.now();
        if (today.isAfter(this.deadline)) {
            return "OVERDUE";
        }
        long daysUntilDeadline = ChronoUnit.DAYS.between(today, this.deadline);
        if (daysUntilDeadline <= 2) {
            return "DUE SOON";
        }
        return "ON TRACK";
    }

    public int getPriorityRank() {
        if (PRIORITY_HIGH.equalsIgnoreCase(this.priority)) return 1;
        if (PRIORITY_MEDIUM.equalsIgnoreCase(this.priority)) return 2;
        if (PRIORITY_LOW.equalsIgnoreCase(this.priority)) return 3;
        return 4;
    }

    @Override
    public int compareTo(Task other) {
        if (other == null) return -1;
        int priorityComparison = Integer.compare(this.getPriorityRank(), other.getPriorityRank());
        if (priorityComparison != 0) {
            return priorityComparison;
        }
        if (this.deadline != null && other.deadline != null) {
            int dateComparison = this.deadline.compareTo(other.deadline);
            if (dateComparison != 0) return dateComparison;
        } else if (this.deadline != null) {
            return -1;
        } else if (other.deadline != null) {
            return 1;
        }
        return Integer.compare(this.taskId, other.taskId);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Task task = (Task) o;
        return taskId == task.taskId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(taskId);
    }

    @Override
    public String toString() {
        return "Task{" +
                "taskId=" + taskId +
                ", taskTitle='" + taskTitle + '\'' +
                ", employeeId=" + employeeId +
                ", priority='" + priority + '\'' +
                ", deadline=" + deadline +
                ", status='" + status + '\'' +
                ", completionDate=" + completionDate +
                '}';
    }
}
