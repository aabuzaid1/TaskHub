package database.DAO;

import database.DatabaseConnection;
import model.Task;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TaskDAO {

    public boolean insertTask(Task task) {
        String sql = "INSERT INTO tasks (title, description, status, workspace_id) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            pstmt.setString(1, task.getTitle());
            pstmt.setString(2, task.getDescription());
            pstmt.setString(3, task.getStatus());
            pstmt.setInt(4, task.getWorkspaceId());
            
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        task.setId(generatedKeys.getInt(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            System.out.println("Error inserting task: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    public List<Task> getTasksByWorkspace(int workspaceId) {
        List<Task> tasks = new ArrayList<>();
        String sql = "SELECT * FROM tasks WHERE workspace_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, workspaceId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    tasks.add(new Task(
                        rs.getInt("id"),
                        rs.getString("title"),
                        rs.getString("description"),
                        rs.getString("status"),
                        rs.getInt("workspace_id")
                    ));
                }
            }
        } catch (SQLException e) {
            System.out.println("Error getting tasks by workspace: " + e.getMessage());
            e.printStackTrace();
        }
        return tasks;
    }

    public boolean updateTaskStatus(int taskId, String status) {
        String sql = "UPDATE tasks SET status = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, status);
            pstmt.setInt(2, taskId);
            
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("Error updating task status: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    public boolean deleteTask(int taskId) {
        String sql = "DELETE FROM tasks WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, taskId);
            return pstmt.executeUpdate() > 0;
            
        } catch (SQLException e) {
            System.out.println("Error deleting task: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    public int getTasksCountByUser(int userId) {
        String sql = "SELECT COUNT(*) FROM tasks t JOIN workspaces w ON t.workspace_id = w.id WHERE w.user_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            System.out.println("Error counting user tasks: " + e.getMessage());
            e.printStackTrace();
        }
        return 0;
    }

    public int getCompletedTasksCountByUser(int userId) {
        String sql = "SELECT COUNT(*) FROM tasks t JOIN workspaces w ON t.workspace_id = w.id WHERE w.user_id = ? AND t.status = 'COMPLETED'";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            System.out.println("Error counting user completed tasks: " + e.getMessage());
            e.printStackTrace();
        }
        return 0;
    }

    public List<Task> getUrgentTasksByUser(int userId) {
        // Just return the first 10 pending tasks belonging to the user as urgent tasks
        List<Task> urgentTasks = new ArrayList<>();
        String sql = "SELECT t.* FROM tasks t JOIN workspaces w ON t.workspace_id = w.id WHERE w.user_id = ? AND t.status = 'PENDING' LIMIT 10";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    urgentTasks.add(new Task(
                        rs.getInt("id"),
                        rs.getString("title"),
                        rs.getString("description"),
                        rs.getString("status"),
                        rs.getInt("workspace_id")
                    ));
                }
            }
        } catch (SQLException e) {
            System.out.println("Error getting urgent tasks: " + e.getMessage());
            e.printStackTrace();
        }
        return urgentTasks;
    }
}
