package com.taskmanagement;

import com.taskmanagement.servlet.DashboardServlet;
import com.taskmanagement.servlet.EmployeeServlet;
import com.taskmanagement.servlet.TaskServlet;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.resource.Resource;

import java.io.File;

/**
 * Embedded Web Server Launcher for Employee Task Management System.
 * Enables zero-configuration standalone running via:
 * - mvn compile exec:java
 * - Direct execution in VS Code / IntelliJ / Eclipse (Right-click -> Run)
 */
public class MainServer {

    private static final int DEFAULT_PORT = 8080;

    public static void main(String[] args) throws Exception {
        int port = DEFAULT_PORT;
        String portEnv = System.getenv("PORT");
        if (portEnv != null && !portEnv.trim().isEmpty()) {
            try {
                port = Integer.parseInt(portEnv.trim());
            } catch (NumberFormatException ignored) {}
        }

        Server server = new Server(port);

        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/");

        // Locate webapp directory for static assets
        File webappDir = new File("src/main/webapp");
        if (!webappDir.exists()) {
            webappDir = new File("webapp");
        }
        if (webappDir.exists()) {
            context.setBaseResource(Resource.newResource(webappDir.getAbsolutePath()));
        }

        // Register API Servlets
        context.addServlet(new ServletHolder(new EmployeeServlet()), "/api/employees/*");
        context.addServlet(new ServletHolder(new TaskServlet()), "/api/tasks/*");
        context.addServlet(new ServletHolder(new DashboardServlet()), "/api/dashboard/*");

        // Register DefaultServlet for static files (HTML, CSS, JS)
        ServletHolder defaultHolder = new ServletHolder("default", DefaultServlet.class);
        defaultHolder.setInitParameter("dirAllowed", "false");
        defaultHolder.setInitParameter("welcomeServlets", "false");
        defaultHolder.setInitParameter("redirectWelcome", "true");
        context.addServlet(defaultHolder, "/");
        context.setWelcomeFiles(new String[]{"index.html"});

        server.setHandler(context);

        System.out.println("==================================================================");
        System.out.println(" Employee Task Management System - Server Starting");
        System.out.println(" Server URL: http://localhost:" + port);
        System.out.println(" Press Ctrl+C in terminal to stop server.");
        System.out.println("==================================================================");

        server.start();
        server.join();
    }
}
