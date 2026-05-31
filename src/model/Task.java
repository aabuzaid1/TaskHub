package model;

public class Task {
    private int id;
    private String title;
    private String description;
    private String status; // 'PENDING' or 'COMPLETED'
    private int workspaceId;

    // Constructors
    public Task() {}

    public Task(int id, String title, String description, String status, int workspaceId) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.status = status;
        this.workspaceId = workspaceId;
    }

    public Task(String title, String description, int workspaceId) {
        this.title = title;
        this.description = description;
        this.status = "PENDING"; // Default value
        this.workspaceId = workspaceId;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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
}
