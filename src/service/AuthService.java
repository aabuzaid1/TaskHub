package service;

import database.DAO.UserDAO;
import model.User;
import util.PasswordHasher;
import util.SessionManger;

public class AuthService {
    private final UserDAO userDAO = new UserDAO();

    public boolean login(String username, String password) {
        if (username == null || password == null) return false;
        
        User user = userDAO.getUserByUsername(username);
        if (user != null) {
            String hashedInput = PasswordHasher.hashPassword(password);
            if (user.getPassword().equals(hashedInput) || user.getPassword().equals(password)) {
                SessionManger.setCurrentUser(user);
                return true;
            }
        }
        return false;
    }

    public boolean register(String username, String password) {
        return register(username, password, "👨‍💻 Developer");
    }

    public boolean register(String username, String password, String avatar) {
        if (username == null || password == null) return false;
        
        // Check if user already exists
        if (userDAO.getUserByUsername(username) != null) {
            System.out.println("Registration failed: Username already exists.");
            return false;
        }

        String hashedPassword = PasswordHasher.hashPassword(password);
        User newUser = new User(username, hashedPassword, avatar);
        return userDAO.insertUser(newUser);
    }
}
