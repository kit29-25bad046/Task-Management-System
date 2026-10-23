-- ==========================================================
-- Employee Task Management System Database Schema
-- Database: employee_task_management
-- ==========================================================

CREATE DATABASE IF NOT EXISTS employee_task_management;
USE employee_task_management;

-- 1. Employees Table
CREATE TABLE IF NOT EXISTS employees (
    employee_id INT PRIMARY KEY,
    employee_name VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL,
    department VARCHAR(100) NOT NULL
);

-- 2. Tasks Table
CREATE TABLE IF NOT EXISTS tasks (
    task_id INT PRIMARY KEY,
    task_title VARCHAR(200) NOT NULL,
    description TEXT,
    employee_id INT NOT NULL,
    priority VARCHAR(20) NOT NULL,
    deadline DATE NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    completion_date DATE DEFAULT NULL,
    FOREIGN KEY (employee_id) REFERENCES employees(employee_id) ON DELETE CASCADE ON UPDATE CASCADE
);

-- Seed Sample Data (Optional, provided for instant testing)
INSERT IGNORE INTO employees (employee_id, employee_name, email, department) VALUES
(101, 'Alice Johnson', 'alice.johnson@company.com', 'Engineering'),
(102, 'Bob Smith', 'bob.smith@company.com', 'Product'),
(103, 'Carol Davis', 'carol.davis@company.com', 'Quality Assurance'),
(104, 'David Wilson', 'david.wilson@company.com', 'DevOps');

INSERT IGNORE INTO tasks (task_id, task_title, description, employee_id, priority, deadline, status, completion_date) VALUES
(1001, 'Implement Auth Middleware', 'Create JWT authentication filters for REST endpoints', 101, 'HIGH', DATE_ADD(CURRENT_DATE, INTERVAL 2 DAY), 'IN PROGRESS', NULL),
(1002, 'Design System Architecture', 'Prepare high level design docs for microservices', 102, 'HIGH', DATE_ADD(CURRENT_DATE, INTERVAL 4 DAY), 'PENDING', NULL),
(1003, 'Automated Regression Suite', 'Write JUnit and integration tests for task module', 103, 'MEDIUM', DATE_ADD(CURRENT_DATE, INTERVAL 5 DAY), 'PENDING', NULL),
(1004, 'Configure CI/CD Pipelines', 'Set up GitHub Actions workflow for automated deployment', 104, 'MEDIUM', DATE_SUB(CURRENT_DATE, INTERVAL 1 DAY), 'PENDING', NULL),
(1005, 'Database Indexing Optimization', 'Optimize slow queries on tasks and employee tables', 101, 'LOW', DATE_ADD(CURRENT_DATE, INTERVAL 10 DAY), 'PENDING', NULL),
(1006, 'Legacy Bug Patching', 'Fix null pointer exception during session termination', 101, 'HIGH', DATE_SUB(CURRENT_DATE, INTERVAL 3 DAY), 'COMPLETED', DATE_SUB(CURRENT_DATE, INTERVAL 3 DAY));
