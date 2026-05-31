package controller;

import service.WorkSpaceService;
import model.Task;
import model.Workspace;
import util.SessionManger;
import java.util.List;

public class DashboardContoller {
    private final WorkSpaceService workSpaceService = new WorkSpaceService();

    public int getActiveWorkspacesCount() {
        if (!SessionManger.isLoggedIn()) return 0;
        List<Workspace> list = workSpaceService.getWorkspacesForUser(SessionManger.getCurrentUser().getId());
        return list.size();
    }

    public int getTodayTasksCount() {
        if (!SessionManger.isLoggedIn()) return 0;
        return workSpaceService.getTasksCount(SessionManger.getCurrentUser().getId());
    }

    public int getCompletedTasksCount() {
        if (!SessionManger.isLoggedIn()) return 0;
        return workSpaceService.getCompletedTasksCount(SessionManger.getCurrentUser().getId());
    }

    public List<Task> getUrgentTasks() {
        if (!SessionManger.isLoggedIn()) return java.util.Collections.emptyList();
        return workSpaceService.getUrgentTasks(SessionManger.getCurrentUser().getId());
    }
}
