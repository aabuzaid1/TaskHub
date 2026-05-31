package util;

import model.User;
import model.Workspace;

public class SessionManger {
    private static User currentUser = null;
    private static Workspace activeWorkspace = null;

    public static User getCurrentUser() {
        return currentUser;
    }

    public static void setCurrentUser(User user) {
        currentUser = user;
    }

    public static Workspace getActiveWorkspace() {
        return activeWorkspace;
    }

    public static void setActiveWorkspace(Workspace workspace) {
        activeWorkspace = workspace;
    }

    public static boolean isLoggedIn() {
        return currentUser != null;
    }

    public static void logout() {
        currentUser = null;
        activeWorkspace = null;
    }
}
