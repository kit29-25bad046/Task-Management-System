# Employee Task Management System Using Java Collections and JDBC

A modern, full-stack enterprise web application designed to demonstrate the real-world application of **Java Collections Framework (`ArrayList`, `HashMap`, `PriorityQueue`)** combined with **JDBC PreparedStatement persistence**, a **MySQL database**, and a **modern, responsive web dashboard**.

---

## 1. Project Overview

The **Employee Task Management System** bridges database persistence and in-memory Java data structures:
- **Full-Stack Connectivity:** Employees and tasks are persistently saved and retrieved from MySQL via JDBC.
- **Java Collections in Action:**
  - `ArrayList<Task>` is used to store, iterate, and process tasks retrieved from MySQL.
  - `HashMap<Integer, ArrayList<Task>>` provides $O(1)$ relational indexing to map employee IDs directly to their assigned task collections.
  - `PriorityQueue<Task>` implements an active min-heap prioritizing urgent tasks (`HIGH` > `MEDIUM` > `LOW`) with tie-breaking based on earliest deadline date.
- **Enterprise UI:** Features an interactive dashboard with real-time statistics, deadline tracking (`ON TRACK`, `DUE SOON`, `OVERDUE`, `COMPLETED`), employee directory management, and task assignment.

---

## 2. Technologies Used

| Layer | Technology | Details |
|---|---|---|
| **Language** | Java 21 LTS | Core business logic, Servlets, Collections |
| **Persistence** | JDBC (Java Database Connectivity) | `PreparedStatement`, `ResultSet`, connection management |
| **Database** | MySQL Server 8.0+ | Relational schema with Foreign Keys and ON DELETE CASCADE |
| **Build & Dependencies** | Apache Maven | Project lifecycle management, dependencies, WAR packaging |
| **Web Container** | Jakarta Servlet 5.0 / Embedded Jetty 11 | REST APIs, static file routing, zero-install execution |
| **Serialization** | Google Gson 2.10.1 | JSON request/response parsing with `LocalDate` adapters |
| **Frontend UI** | HTML5, Modern CSS, Vanilla JavaScript | Responsive glassmorphic dashboard, modals, real-time filters |

---

## 3. Frontend Architecture

The frontend is a single-page dashboard application built with HTML5, Vanilla CSS, and asynchronous JavaScript:
- **Dashboard Overview:** KPI counters for Total Employees, Total Tasks, Pending, In Progress, Completed, Overdue, and Completion Rate.
- **Employee Directory:** Add, edit, search, and delete employees with real-time table rendering.
- **Task Assignment:** Form with automated validation ensuring unique Task IDs, verified employee existence, priority levels, and valid deadline dates.
- **All Tasks View:** Dynamic table with inline status toggles, keyword search, priority filter, and status filter.
- **Priority Queue View:** Visual inspection of tasks dynamically prioritized by Java's `PriorityQueue<Task>`.
- **Employee Map View:** Interactive relational view powered by `HashMap<Integer, ArrayList<Task>>` displaying assignments per team member.
- **Deadline Tracking:** Visual badges automatically calculated relative to current date:
  - `ON TRACK`: Deadline > 2 days ahead
  - `DUE SOON`: Deadline within 2 days
  - `OVERDUE`: Deadline is in the past and status is not COMPLETED
  - `COMPLETED`: Task finished, records completion date in MySQL

---

## 4. Backend Architecture

```
com.taskmanagement
├── MainServer.java             # Embedded Jetty launcher for one-click startup
├── config
│   └── DBConnection.java       # Centralized JDBC connection manager with schema initialization
├── dao
│   ├── EmployeeDAO.java        # PreparedStatement CRUD operations for employees
│   └── TaskDAO.java            # PreparedStatement CRUD and query filtering for tasks
├── model
│   ├── Employee.java           # Employee domain model
│   └── Task.java               # Task domain model (implements Comparable<Task> for PriorityQueue)
├── service
│   ├── EmployeeManager.java    # Business validation and employee services
│   └── TaskManager.java        # Demonstrates ArrayList, HashMap, and PriorityQueue operations
└── servlet
    ├── BaseServlet.java        # Abstract servlet with Gson LocalDate serialization & error helpers
    ├── DashboardServlet.java   # /api/dashboard endpoint (KPI metrics & MySQL health)
    ├── EmployeeServlet.java    # /api/employees endpoint (CRUD & search)
    └── TaskServlet.java        # /api/tasks endpoint (CRUD, filters, priority queue, employee map)
```

---

## 5. Database Schema

The system uses a dedicated MySQL database: `employee_task_management`.

```sql
CREATE DATABASE IF NOT EXISTS employee_task_management;
USE employee_task_management;

-- Employees Table
CREATE TABLE IF NOT EXISTS employees (
    employee_id INT PRIMARY KEY,
    employee_name VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL,
    department VARCHAR(100) NOT NULL
);

-- Tasks Table
CREATE TABLE IF NOT EXISTS tasks (
    task_id INT PRIMARY KEY,
    task_title VARCHAR(200) NOT NULL,
    description TEXT,
    employee_id INT NOT NULL,
    priority VARCHAR(20) NOT NULL,
    deadline DATE NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    completion_date DATE DEFAULT NULL,
    FOREIGN KEY (employee_id) REFERENCES employees(employee_id) 
        ON DELETE CASCADE ON UPDATE CASCADE
);
```

---

## 6. JDBC Connection Setup

Database configurations are centralized in [src/main/resources/database.properties](file:///c:/Users/Lenovo/Task%20management%20system/src/main/resources/database.properties):

```properties
db.url=jdbc:mysql://localhost:3306/employee_task_management?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&createDatabaseIfNotExist=true
db.username=root
db.password=YOUR_MYSQL_PASSWORD_HERE
db.driver=com.mysql.cj.jdbc.Driver
```

You can also pass environment variables without modifying code:
- `DB_URL`
- `DB_USERNAME`
- `DB_PASSWORD`

All queries use `PreparedStatement` to prevent SQL injection:
```java
String sql = "INSERT INTO tasks (task_id, task_title, description, employee_id, priority, deadline, status) VALUES (?, ?, ?, ?, ?, ?, ?)";
try (Connection conn = DBConnection.getConnection();
     PreparedStatement pstmt = conn.prepareStatement(sql)) {
    pstmt.setInt(1, task.getTaskId());
    pstmt.setString(2, task.getTaskTitle());
    // ...
    pstmt.executeUpdate();
}
```

---

## 7. MySQL Database Creation

Open your MySQL terminal or MySQL Workbench and run:

```bash
mysql -u root -p < schema.sql
```

Alternatively, open MySQL CLI:
```sql
SOURCE c:/Users/Lenovo/Task management system/schema.sql;
```

---

## 8. Maven Setup & Build

Verify Maven is installed and compile the project:

```bash
# Check Maven installation
mvn -version

# Clean and package the application
mvn clean package
```

The build produces a deployable WAR package:
`target/employee-task-management.war`.

---

## 9. How to Run the Project

You can run the application through any of the following convenient methods:

### Option A: Maven CLI One-Click Run (Recommended)
An embedded Jetty 11 server is configured into the project. Start it directly with:
```bash
mvn compile exec:java
```
Or with the Jetty Maven plugin:
```bash
mvn jetty:run
```
Then open your web browser at:
👉 **`http://localhost:8080`**

---

### Option B: Running in VS Code
1. Open the folder `c:\Users\Lenovo\Task management system` in VS Code.
2. Ensure the **Extension Pack for Java** is installed.
3. Open `src/main/java/com/taskmanagement/MainServer.java`.
4. Click the **Run** button above `public static void main` (or press `F5`).
5. Open your browser at `http://localhost:8080`.

---

### Option C: Running in IntelliJ IDEA
1. Open IntelliJ IDEA and select **File -> Open...** -> select `c:\Users\Lenovo\Task management system`.
2. IntelliJ will detect `pom.xml` and import dependencies automatically.
3. In the project tree, navigate to:
   `src/main/java/com/taskmanagement/MainServer.java`.
4. Right-click `MainServer.java` and choose **Run 'MainServer.main()'**.
5. Alternatively, under the Maven tool window on the right, expand **Plugins -> jetty -> jetty:run**.
6. Access the dashboard at `http://localhost:8080`.

---

### Option D: Running in Eclipse IDE
1. Open Eclipse -> **File -> Import... -> Existing Maven Projects**.
2. Select the directory `c:\Users\Lenovo\Task management system`.
3. Right-click the project -> **Run As -> Java Application** -> Select `MainServer`.
4. (Optional WAR Deployment): Right-click project -> **Run As -> Run on Server** -> Select Apache Tomcat 10+.

---

## 10. Explanation of `ArrayList<Task>`

### What is it?
`ArrayList` is a resizable-array implementation of the `List` interface in `java.util`. It provides dynamic memory allocation and constant-time $O(1)$ random positional access.

### How it is used in this project:
1. When records are fetched from MySQL via JDBC `ResultSet`, they are transformed into domain models and collected into an `ArrayList<Task>`:
   ```java
   public ArrayList<Task> getAllTasks() throws SQLException {
       List<Task> rawList = taskDAO.getAllTasks();
       return new ArrayList<>(rawList);
   }
   ```
2. The `ArrayList<Task>` preserves insertion and retrieval order, simplifies iteration for computing statistical metrics (counting overdue, pending, and completed tasks), and allows sequential filtering and keyword search across attributes.

---

## 11. Explanation of `HashMap<Integer, ArrayList<Task>>`

### What is it?
`HashMap` is a hash-table based implementation of the `Map` interface. It stores key-value pairs and offers average $O(1)$ time complexity for `get()` and `put()` operations.

### How it is used in this project:
1. In `TaskManager.java`, tasks retrieved from MySQL are mapped by their foreign key `employee_id`:
   ```java
   public HashMap<Integer, ArrayList<Task>> getEmployeeTaskMap() throws SQLException {
       ArrayList<Task> allTasks = getAllTasks();
       HashMap<Integer, ArrayList<Task>> taskMap = new HashMap<>();

       for (Task task : allTasks) {
           int empId = task.getEmployeeId();
           taskMap.computeIfAbsent(empId, k -> new ArrayList<>()).add(task);
       }
       return taskMap;
   }
   ```
2. When the user selects an employee on the **Employee Map** tab, the application queries `taskMap.get(employeeId)` in constant time $O(1)$, instantly retrieving all tasks belonging to that employee without issuing repetitive SQL queries.

---

## 12. Explanation of `PriorityQueue<Task>`

### What is it?
`PriorityQueue` is an unbounded priority heap based on a priority queue data structure. Its elements are ordered according to their **natural ordering** (defined by `Comparable<T>`) or by a provided `Comparator`. The head of the queue is the *least* element with respect to the specified ordering.

### How it is used in this project:
1. `Task.java` implements `Comparable<Task>` to enforce two-tiered dispatch prioritization:
   - **Tier 1 (Priority Rank):** `HIGH` (Rank 1) > `MEDIUM` (Rank 2) > `LOW` (Rank 3).
   - **Tier 2 (Deadline Tie-Breaker):** If two tasks share the same priority, the task with the earlier deadline is prioritized first.
   ```java
   @Override
   public int compareTo(Task other) {
       int priorityComparison = Integer.compare(this.getPriorityRank(), other.getPriorityRank());
       if (priorityComparison != 0) {
           return priorityComparison;
       }
       // If priorities match, sort by earliest deadline date
       return this.deadline.compareTo(other.deadline);
   }
   ```
2. In `TaskManager.java`, pending and in-progress tasks are placed into the `PriorityQueue<Task>`:
   ```java
   public PriorityQueue<Task> getPriorityTaskQueue() throws SQLException {
       ArrayList<Task> allTasks = getAllTasks();
       PriorityQueue<Task> queue = new PriorityQueue<>();
       for (Task task : allTasks) {
           if (!Task.STATUS_COMPLETED.equalsIgnoreCase(task.getStatus())) {
               queue.offer(task);
           }
       }
       return queue;
   }
   ```
3. The queue is polled to deliver a sorted dispatch queue (`HIGH` priority items with tight deadlines always appear at Rank #1).

---

## 13. Verification and Testing

To verify the installation and build the complete WAR:
```bash
mvn clean package
```
All dependencies, compiled classes, and static web resources are assembled cleanly into:
`target/employee-task-management.war`.
