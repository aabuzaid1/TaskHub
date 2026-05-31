package database.DAO;

import database.DatabaseConnection;
import model.PathLink;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for PathLink (Slides/Folders).
 * Written in a simple student style.
 */
public class PAthLinkDAO {

    public boolean insertPathLink(PathLink link) {
        String sql = "INSERT INTO path_links (name, file_path, status, workspace_id) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            pstmt.setString(1, link.getName());
            pstmt.setString(2, link.getFilePath());
            pstmt.setString(3, link.getStatus());
            pstmt.setInt(4, link.getWorkspaceId());
            
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        link.setId(generatedKeys.getInt(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            System.out.println("Error inserting path link: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    public List<PathLink> getPathLinksByWorkspace(int workspaceId) {
        List<PathLink> links = new ArrayList<>();
        String sql = "SELECT * FROM path_links WHERE workspace_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, workspaceId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    links.add(new PathLink(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("file_path"),
                        rs.getString("status"),
                        rs.getInt("workspace_id")
                    ));
                }
            }
        } catch (SQLException e) {
            System.out.println("Error getting path links: " + e.getMessage());
            e.printStackTrace();
        }
        return links;
    }

    public boolean updatePathLinkStatus(int linkId, String status) {
        String sql = "UPDATE path_links SET status = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, status);
            pstmt.setInt(2, linkId);
            return pstmt.executeUpdate() > 0;
            
        } catch (SQLException e) {
            System.out.println("Error updating path link status: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    public boolean deletePathLink(int linkId) {
        String sql = "DELETE FROM path_links WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, linkId);
            return pstmt.executeUpdate() > 0;
            
        } catch (SQLException e) {
            System.out.println("Error deleting path link: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }
}
