package controller;

import service.AuthService;
import util.ValidationUtil;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

public class LoginController {
    private final AuthService authService = new AuthService();

    public boolean handleLogin(String username, String password) {
        if (ValidationUtil.isEmpty(username) || ValidationUtil.isEmpty(password)) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "Please fill in all fields.");
            return false;
        }

        boolean success = authService.login(username, password);
        if (success) {
            System.out.println("Login successful!");
            return true;
        } else {
            showAlert(Alert.AlertType.ERROR, "Login Failed", "Invalid username or password.");
            return false;
        }
    }

    public boolean handleSignUp(String username, String password) {
        return handleSignUp(username, password, "👨‍💻 Developer");
    }

    public boolean handleSignUp(String username, String password, String avatar) {
        if (!ValidationUtil.validateUserInputs(username, password)) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", 
                "Username must be at least 3 characters and password at least 4 characters.");
            return false;
        }

        boolean success = authService.register(username, password, avatar);
        if (success) {
            showAlert(Alert.AlertType.INFORMATION, "Registration Successful", "Account created! You can now log in.");
            return true;
        } else {
            showAlert(Alert.AlertType.ERROR, "Registration Failed", "Username might already be taken.");
            return false;
        }
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
