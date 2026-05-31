package service;

import database.DAO.WorkspaceDAO;
import database.DAO.TaskDAO;
import database.DAO.PAthLinkDAO;
import model.Workspace;
import model.Task;
import model.PathLink;
import java.util.List;

/**
 * Service class for Workspaces, Tasks, and Slide/Folder path links.
 * Written in a simple student style.
 */
public class WorkSpaceService {
    private final WorkspaceDAO workspaceDAO = new WorkspaceDAO();
    private final TaskDAO taskDAO = new TaskDAO();
    private final PAthLinkDAO pathLinkDAO = new PAthLinkDAO();

    // Workspace methods
    public boolean createWorkspace(String name, String description, int userId) {
        Workspace ws = new Workspace(name, description, userId);
        return workspaceDAO.insertWorkspace(ws);
    }

    public List<Workspace> getWorkspacesForUser(int userId) {
        return workspaceDAO.getWorkspacesByUser(userId);
    }

    public boolean deleteWorkspace(int workspaceId) {
        return workspaceDAO.deleteWorkspace(workspaceId);
    }

    // Task methods
    public boolean addTask(String title, String description, int workspaceId) {
        Task task = new Task(title, description, workspaceId);
        return taskDAO.insertTask(task);
    }

    public List<Task> getTasksForWorkspace(int workspaceId) {
        return taskDAO.getTasksByWorkspace(workspaceId);
    }

    public boolean toggleTaskStatus(Task task) {
        String newStatus = task.getStatus().equals("PENDING") ? "COMPLETED" : "PENDING";
        boolean success = taskDAO.updateTaskStatus(task.getId(), newStatus);
        if (success) {
            task.setStatus(newStatus);
        }
        return success;
    }

    public boolean deleteTask(int taskId) {
        return taskDAO.deleteTask(taskId);
    }

    // Path Link methods (Slides and Folders)
    public boolean addPathLink(String name, String filePath, int workspaceId) {
        PathLink link = new PathLink(name, filePath, workspaceId);
        return pathLinkDAO.insertPathLink(link);
    }

    public List<PathLink> getPathLinksForWorkspace(int workspaceId) {
        return pathLinkDAO.getPathLinksByWorkspace(workspaceId);
    }

    public boolean togglePathLinkStatus(PathLink link) {
        String newStatus = link.getStatus().equals("PENDING") ? "COMPLETED" : "PENDING";
        boolean success = pathLinkDAO.updatePathLinkStatus(link.getId(), newStatus);
        if (success) {
            link.setStatus(newStatus);
        }
        return success;
    }

    public boolean deletePathLink(int linkId) {
        return pathLinkDAO.deletePathLink(linkId);
    }

    // Stats for Student Dashboard
    public int getTasksCount(int userId) {
        return taskDAO.getTasksCountByUser(userId);
    }

    public int getCompletedTasksCount(int userId) {
        return taskDAO.getCompletedTasksCountByUser(userId);
    }

    public List<Task> getUrgentTasks(int userId) {
        return taskDAO.getUrgentTasksByUser(userId);
    }
}
