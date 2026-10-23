package com.taskmanagement.config;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

/**
 * Centralized JDBC Database Connection Manager.
 * Loads configuration from database.properties or environment variables.
 * Provides live connections and initializes schema if tables do not exist.
 */
public class DBConnection {

    private static String dbUrl;
    private static String dbUsername;
    private static String dbPassword;
    private static String dbDriver;
    private static boolean initialized = false;

    static {
        loadConfiguration();
    }

    private static synchronized void loadConfiguration() {
        Properties props = new Properties();
        try (InputStream in = DBConnection.class.getClassLoader().getResourceAsStream("database.properties")) {
            if (in != null) {
                props.load(in);
            }
        } catch (Exception e) {
            System.err.println("[DBConnection] Warning: Could not load database.properties: " + e.getMessage());
        }

        // Environment variables / System properties take precedence over properties file
        dbUrl = System.getenv("DB_URL");
        if (dbUrl == null || dbUrl.trim().isEmpty()) {
            dbUrl = System.getProperty("db.url", props.getProperty("db.url",
                    "jdbc:mysql://localhost:3306/employee_task_management?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&createDatabaseIfNotExist=true"));
        }

        dbUsername = System.getenv("DB_USERNAME");
        if (dbUsername == null || dbUsername.trim().isEmpty()) {
            dbUsername = System.getProperty("db.username", props.getProperty("db.username", "root"));
        }

        dbPassword = System.getenv("DB_PASSWORD");
        if (dbPassword == null) {
            dbPassword = System.getProperty("db.password", props.getProperty("db.password", ""));
        }

        dbDriver = props.getProperty("db.driver", "com.mysql.cj.jdbc.Driver");

        try {
            Class.forName(dbDriver);
        } catch (ClassNotFoundException e) {
            System.err.println("[DBConnection] MySQL JDBC Driver not found: " + e.getMessage());
        }
    }

    /**
     * Obtains a new JDBC Connection to MySQL.
     * @return active java.sql.Connection
     * @throws SQLException if connection fails
     */
    public static Connection getConnection() throws SQLException {
        Connection conn = DriverManager.getConnection(dbUrl, dbUsername, dbPassword);
        if (!initialized) {
            initializeDatabaseTables(conn);
        }
        return conn;
    }

    /**
     * Verifies if MySQL can be reached with current credentials.
     */
    public static boolean testConnection() {
        try (Connection conn = getConnection()) {
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            System.err.println("[DBConnection] Test connection failed: " + e.getMessage());
            return false;
        }
    }

    /**
     * Initializes tables from schema.sql if they do not exist.
     */
    private static synchronized void initializeDatabaseTables(Connection conn) {
        if (initialized) return;
        try (InputStream in = DBConnection.class.getClassLoader().getResourceAsStream("schema.sql")) {
            if (in != null) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
                StringBuilder sqlBuilder = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    line = line.trim();
                    if (line.startsWith("--") || line.isEmpty()) continue;
                    sqlBuilder.append(line).append(" ");
                }
                String[] statements = sqlBuilder.toString().split(";");
                try (Statement stmt = conn.createStatement()) {
                    for (String sql : statements) {
                        if (!sql.trim().isEmpty()) {
                            stmt.execute(sql.trim());
                        }
                    }
                }
                initialized = true;
                System.out.println("[DBConnection] Schema initialization verified successfully.");
            }
        } catch (Exception e) {
            System.err.println("[DBConnection] Note on schema initialization: " + e.getMessage());
        }
    }

    public static String getDbUrl() {
        return dbUrl;
    }

    public static String getDbUsername() {
        return dbUsername;
    }

    public static void setPassword(String password) {
        dbPassword = password;
    }
}
