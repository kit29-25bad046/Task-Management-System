-- ==========================================================
-- Employee Task Management System Database Schema
-- Database: employee_task_management
-- ==========================================================

CREATE TABLE IF NOT EXISTS employees (
    employee_id INT PRIMARY KEY,
    employee_name VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL,
    department VARCHAR(100) NOT NULL
);

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
