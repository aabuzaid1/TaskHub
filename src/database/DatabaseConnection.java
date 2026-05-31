package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Database Connection class.
 * Written in a simple student style to manage connections and initialize tables.
 */
public class DatabaseConnection {
    private static final String SERVER_URL = "jdbc:mysql://localhost:3306/";
    private static final String DB_NAME = "taskhub";
    private static final String URL = SERVER_URL + DB_NAME;
    private static final String USER = "root";
    private static final String PASSWORD = "";
    
    private static Connection conn = null;

    public static Connection getConnection() {
        try {
            if (conn == null || conn.isClosed()) {
                Class.forName("com.mysql.cj.jdbc.Driver");
                
                // 1. Automatically create database if it does not exist
                try (Connection tempConn = DriverManager.getConnection(SERVER_URL, USER, PASSWORD);
                     Statement stmt = tempConn.createStatement()) {
                    stmt.executeUpdate("CREATE DATABASE IF NOT EXISTS " + DB_NAME);
                } catch (SQLException e) {
                    System.out.println("Warning: Could not check or create database. Trying to connect anyway...");
                }

                // 2. Connect to the taskhub database
                conn = DriverManager.getConnection(URL, USER, PASSWORD);
            }
        } catch (ClassNotFoundException e) {
            System.out.println("Error: MySQL JDBC Driver not found!");
            e.printStackTrace();
        } catch (SQLException e) {
            System.out.println("Error: Failed to connect to MySQL database! Make sure XAMPP/WAMP is running.");
            e.printStackTrace();
        }
        return conn;
    }

    public static void initDatabase() {
        Connection c = getConnection();
        if (c != null) {
            try (Statement stmt = c.createStatement()) {
                // Table 0: users (Crucial! Creating users table first so workspaces can reference it!)
                String sqlUsers = "CREATE TABLE IF NOT EXISTS users (" +
                        "id INT AUTO_INCREMENT PRIMARY KEY, " +
                        "username VARCHAR(50) NOT NULL UNIQUE, " +
                        "password VARCHAR(255) NOT NULL" +
                        ")";
                stmt.executeUpdate(sqlUsers);

                // Ensure password column is large enough for SHA-256 hashed passwords (64 characters)
                try {
                    stmt.executeUpdate("ALTER TABLE users MODIFY COLUMN password VARCHAR(255) NOT NULL");
                    System.out.println("Ensured users.password column is VARCHAR(255).");
                } catch (SQLException ex) {
                    // Already modified or ignore
                }

                // Table 1: workspaces (representing courses/projects)
                String sqlWorkspaces = "CREATE TABLE IF NOT EXISTS workspaces (" +
                        "id INT AUTO_INCREMENT PRIMARY KEY, " +
                        "name VARCHAR(100) NOT NULL, " +
                        "description VARCHAR(255), " +
                        "user_id INT NOT NULL, " +
                        "FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE" +
                        ")";
                stmt.executeUpdate(sqlWorkspaces);

                // Table 2: tasks
                String sqlTasks = "CREATE TABLE IF NOT EXISTS tasks (" +
                        "id INT AUTO_INCREMENT PRIMARY KEY, " +
                        "title VARCHAR(100) NOT NULL, " +
                        "description VARCHAR(255), " +
                        "status VARCHAR(50) DEFAULT 'PENDING', " +
                        "workspace_id INT NOT NULL, " +
                        "FOREIGN KEY (workspace_id) REFERENCES workspaces(id) ON DELETE CASCADE" +
                        ")";
                stmt.executeUpdate(sqlTasks);

                // Table 3: path_links (Slides and Folder links)
                String sqlPathLinks = "CREATE TABLE IF NOT EXISTS path_links (" +
                        "id INT AUTO_INCREMENT PRIMARY KEY, " +
                        "name VARCHAR(100) NOT NULL, " +
                        "file_path VARCHAR(500) NOT NULL, " +
                        "status VARCHAR(50) DEFAULT 'PENDING', " +
                        "workspace_id INT NOT NULL, " +
                        "FOREIGN KEY (workspace_id) REFERENCES workspaces(id) ON DELETE CASCADE" +
                        ")";
                stmt.executeUpdate(sqlPathLinks);

                // Backwards compatibility check: add status column to path_links if not exists
                try {
                    stmt.executeUpdate("ALTER TABLE path_links ADD COLUMN status VARCHAR(50) DEFAULT 'PENDING'");
                    System.out.println("Checked 'status' column in path_links table.");
                } catch (SQLException ex) {
                    // Column already exists, ignore
                }

                // Backwards compatibility check: add avatar column to users if not exists
                try {
                    stmt.executeUpdate("ALTER TABLE users ADD COLUMN avatar VARCHAR(100) DEFAULT '👨‍💻 Developer'");
                    System.out.println("Checked 'avatar' column in users table.");
                } catch (SQLException ex) {
                    // Column already exists, ignore
                }

                // Table 4: appointments
                String sqlAppointments = "CREATE TABLE IF NOT EXISTS appointments (" +
                        "id INT AUTO_INCREMENT PRIMARY KEY, " +
                        "title VARCHAR(100) NOT NULL, " +
                        "date_time VARCHAR(100) NOT NULL, " +
                        "subject VARCHAR(100), " +
                        "description VARCHAR(255), " +
                        "user_id INT NOT NULL, " +
                        "FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE" +
                        ")";
                stmt.executeUpdate(sqlAppointments);

                // Table 5: project_sessions (Free Work tracker for students)
                String sqlProjectSessions = "CREATE TABLE IF NOT EXISTS project_sessions (" +
                        "id INT AUTO_INCREMENT PRIMARY KEY, " +
                        "project_name VARCHAR(100) NOT NULL, " +
                        "hours_spent DOUBLE NOT NULL, " +
                        "work_date VARCHAR(100) NOT NULL, " +
                        "user_id INT NOT NULL, " +
                        "FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE" +
                        ")";
                stmt.executeUpdate(sqlProjectSessions);

                System.out.println("Database tables initialized successfully!");
            } catch (SQLException e) {
                System.out.println("Error initializing database tables!");
                e.printStackTrace();
            }
        }
    }
}
