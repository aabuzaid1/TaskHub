package model;

/**
 * Model class for PathLink (representing slide files, course documents, or directories).
 * Written in a simple student style.
 */
public class PathLink {
    private int id;
    private String name;
    private String filePath;
    private String status; // 'PENDING' or 'COMPLETED' (to track what is completed/done vs what is left)
    private int workspaceId;

    // Constructors
    public PathLink() {
        this.status = "PENDING";
    }

    public PathLink(int id, String name, String filePath, String status, int workspaceId) {
        this.id = id;
        this.name = name;
        this.filePath = filePath;
        this.status = status != null ? status : "PENDING";
        this.workspaceId = workspaceId;
    }

    public PathLink(String name, String filePath, int workspaceId) {
        this.name = name;
        this.filePath = filePath;
        this.status = "PENDING";
        this.workspaceId = workspaceId;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getWorkspaceId() {
        return workspaceId;
    }

    public void setWorkspaceId(int workspaceId) {
        this.workspaceId = workspaceId;
    }

    @Override
    public String toString() {
        return name + " (" + filePath + ")";
    }
}
