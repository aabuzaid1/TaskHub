package controller;

import service.WorkSpaceService;
import model.Workspace;
import model.Task;
import model.PathLink;
import util.SessionManger;
import java.io.*;
import java.util.List;

/**
 * Controller class for Workspaces (Courses/Projects) and Tasks/Files.
 * Written in a simple student style.
 */
public class WorkspaceController {
    private final WorkSpaceService workSpaceService = new WorkSpaceService();

    // Workspace Operations
    public List<Workspace> getWorkspaces() {
        if (!SessionManger.isLoggedIn()) return java.util.Collections.emptyList();
        return workSpaceService.getWorkspacesForUser(SessionManger.getCurrentUser().getId());
    }

    public boolean createWorkspace(String name, String description) {
        if (name == null || name.trim().isEmpty() || !SessionManger.isLoggedIn()) return false;
        return workSpaceService.createWorkspace(name, description, SessionManger.getCurrentUser().getId());
    }

    public boolean deleteWorkspace(Workspace ws) {
        if (ws == null) return false;
        return workSpaceService.deleteWorkspace(ws.getId());
    }

    // Task Operations
    public List<Task> getTasks(Workspace ws) {
        if (ws == null) return java.util.Collections.emptyList();
        return workSpaceService.getTasksForWorkspace(ws.getId());
    }

    public boolean addTask(String title, String description, Workspace ws) {
        if (title == null || title.trim().isEmpty() || ws == null) return false;
        return workSpaceService.addTask(title, description, ws.getId());
    }

    public boolean toggleTaskStatus(Task task) {
        if (task == null) return false;
        return workSpaceService.toggleTaskStatus(task);
    }

    public boolean deleteTask(Task task) {
        if (task == null) return false;
        return workSpaceService.deleteTask(task.getId());
    }

    // File/Folder Link Operations
    public List<PathLink> getPathLinks(Workspace ws) {
        if (ws == null) return java.util.Collections.emptyList();
        return workSpaceService.getPathLinksForWorkspace(ws.getId());
    }

    public boolean addPathLink(String name, String filePath, Workspace ws) {
        if (name == null || filePath == null || ws == null) return false;
        return workSpaceService.addPathLink(name, filePath, ws.getId());
    }

    public boolean togglePathLinkStatus(PathLink link) {
        if (link == null) return false;
        return workSpaceService.togglePathLinkStatus(link);
    }

    public boolean deletePathLink(PathLink link) {
        if (link == null) return false;
        return workSpaceService.deletePathLink(link.getId());
    }

    // File Explorer & System Launcher
    public File[] listFilesForFolder(File folder) {
        if (folder == null || !folder.exists() || !folder.isDirectory()) {
            return new File[0];
        }
        return folder.listFiles();
    }

    public void openFileInSystem(File file) throws IOException {
        if (file == null || !file.exists()) {
            throw new FileNotFoundException("File not found at: " + (file != null ? file.getAbsolutePath() : "null"));
        }
        
        // Use java.awt.Desktop to open file/folder in Windows Explorer or default app
        if (java.awt.Desktop.isDesktopSupported()) {
            java.awt.Desktop.getDesktop().open(file);
        } else {
            // Fallback command execution in Windows
            new ProcessBuilder("explorer.exe", file.getAbsolutePath()).start();
        }
    }

    // File Handling (BufferedReader / BufferedWriter) - As per Lab 16
    public String readFileContent(File file) throws IOException {
        if (file == null || !file.exists() || file.isDirectory()) return "";
        
        StringBuilder content = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
        }
        return content.toString();
    }

    public void writeFileContent(File file, String text) throws IOException {
        if (file == null || file.isDirectory()) return;
        
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write(text);
        }
    }
}
