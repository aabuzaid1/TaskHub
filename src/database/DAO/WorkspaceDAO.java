package database.DAO;

import database.DatabaseConnection;
import model.Workspace;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class WorkspaceDAO {

    public boolean insertWorkspace(Workspace workspace) {
        String sql = "INSERT INTO workspaces (name, description, user_id) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            pstmt.setString(1, workspace.getName());
            pstmt.setString(2, workspace.getDescription());
            pstmt.setInt(3, workspace.getUserId());
            
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        workspace.setId(generatedKeys.getInt(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            System.out.println("Error inserting workspace: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    public List<Workspace> getWorkspacesByUser(int userId) {
        List<Workspace> workspaces = new ArrayList<>();
        String sql = "SELECT * FROM workspaces WHERE user_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    workspaces.add(new Workspace(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("description"),
                        rs.getInt("user_id")
                    ));
                }
            }
        } catch (SQLException e) {
            System.out.println("Error getting workspaces: " + e.getMessage());
            e.printStackTrace();
        }
        return workspaces;
    }

    public boolean deleteWorkspace(int workspaceId) {
        String sql = "DELETE FROM workspaces WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, workspaceId);
            return pstmt.executeUpdate() > 0;
            
        } catch (SQLException e) {
            System.out.println("Error deleting workspace: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    public Workspace getWorkspaceById(int workspaceId) {
        String sql = "SELECT * FROM workspaces WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, workspaceId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new Workspace(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("description"),
                        rs.getInt("user_id")
                    );
                }
            }
        } catch (SQLException e) {
            System.out.println("Error getting workspace by ID: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }
}
