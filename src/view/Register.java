package view;

import controller.LoginController;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import java.util.ArrayList;
import java.util.List;

public class Register extends Application {
    private final LoginController loginController = new LoginController();
    private String selectedAvatar = "👨‍💻 Developer"; // Default avatar selection
    private final List<Button> avatarButtons = new ArrayList<>();

    @Override
    public void start(Stage primaryStage) {
        // UI Header
        Label title = new Label("TaskHub");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 36));
        title.setStyle("-fx-text-fill: #3498DB;");

        Label subtitle = new Label("Join the Premier Student Lab & Course Hub");
        subtitle.setFont(Font.font("Arial", 14));
        subtitle.setStyle("-fx-text-fill: #BDC3C7;");

        // Inputs
        TextField username = new TextField();
        username.setPromptText("Enter Username");
        username.setPrefHeight(40);
        username.setStyle("-fx-font-size: 13; -fx-background-radius: 5; -fx-padding: 10; -fx-background-color: #34495E; -fx-text-fill: white;");

        PasswordField password = new PasswordField();
        password.setPromptText("Enter Password");
        password.setPrefHeight(40);
        password.setStyle("-fx-font-size: 13; -fx-background-radius: 5; -fx-padding: 10; -fx-background-color: #34495E; -fx-text-fill: white;");

        PasswordField confirmPassword = new PasswordField();
        confirmPassword.setPromptText("Confirm Password");
        confirmPassword.setPrefHeight(40);
        confirmPassword.setStyle("-fx-font-size: 13; -fx-background-radius: 5; -fx-padding: 10; -fx-background-color: #34495E; -fx-text-fill: white;");

        // Avatar Selection Label
        Label avatarLabel = new Label("Choose Your Profile Avatar / Role:");
        avatarLabel.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        avatarLabel.setStyle("-fx-text-fill: #ECF0F1;");

        // Horizontal Box for Custom Avatars
        HBox avatarBox = new HBox(8);
        avatarBox.setAlignment(Pos.CENTER);

        String[] avatars = {"👨‍💻 Developer", "👩‍🎓 Scholar", "🎨 Designer", "🚀 Explorer", "📚 Scholar"};
        for (String av : avatars) {
            Button avBtn = new Button(av);
            avBtn.setPrefHeight(35);
            avBtn.setFont(Font.font("Arial", FontWeight.BOLD, 12));
            
            // Set styles based on default selection
            if (av.equals(selectedAvatar)) {
                avBtn.setStyle("-fx-background-color: #2ECC71; -fx-text-fill: white; -fx-cursor: hand; -fx-background-radius: 5; -fx-border-color: #27AE60; -fx-border-width: 2; -fx-border-radius: 5;");
            } else {
                avBtn.setStyle("-fx-background-color: #34495E; -fx-text-fill: #BDC3C7; -fx-cursor: hand; -fx-background-radius: 5;");
            }

            avBtn.setOnAction(e -> {
                selectedAvatar = av;
                updateAvatarSelections();
            });

            avatarButtons.add(avBtn);
            avatarBox.getChildren().add(avBtn);
        }

        // Action Buttons
        Button signUpBtn = new Button("Register Account");
        signUpBtn.setPrefWidth(160);
        signUpBtn.setPrefHeight(40);
        signUpBtn.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        signUpBtn.setStyle("-fx-background-color: #2ECC71; -fx-text-fill: white; -fx-cursor: hand; -fx-background-radius: 5;");

        Button backBtn = new Button("Back to Login");
        backBtn.setPrefWidth(120);
        backBtn.setPrefHeight(40);
        backBtn.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        backBtn.setStyle("-fx-background-color: #7F8C8D; -fx-text-fill: white; -fx-cursor: hand; -fx-background-radius: 5;");

        // Action Handlers
        signUpBtn.setOnAction(e -> {
            String u = username.getText();
            String p = password.getText();
            String cp = confirmPassword.getText();

            if (u.trim().isEmpty() || p.isEmpty() || cp.isEmpty()) {
                Alert alert = new Alert(Alert.AlertType.WARNING, "Please fill in all registration fields!");
                alert.show();
                return;
            }

            if (!p.equals(cp)) {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Passwords do not match! Please verify.");
                alert.show();
                return;
            }

            boolean success = loginController.handleSignUp(u.trim(), p, selectedAvatar);
            if (success) {
                // Switch back to login page
                Login login = new Login();
                login.start(primaryStage);
            }
        });

        backBtn.setOnAction(e -> {
            Login login = new Login();
            login.start(primaryStage);
        });

        // Layout Structure
        VBox root = new VBox(20);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(40));
        root.setStyle("-fx-background-color: #2C3E50;");

        VBox inputBox = new VBox(12);
        inputBox.setPrefWidth(320);
        inputBox.setMaxWidth(320);
        inputBox.getChildren().addAll(username, password, confirmPassword);

        HBox btnBox = new HBox(15);
        btnBox.setAlignment(Pos.CENTER);
        btnBox.getChildren().addAll(signUpBtn, backBtn);

        root.getChildren().addAll(title, subtitle, inputBox, avatarLabel, avatarBox, btnBox);

        Scene scene = new Scene(root, 650, 520);
        primaryStage.setTitle("TaskHub - Register");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void updateAvatarSelections() {
        for (Button btn : avatarButtons) {
            if (btn.getText().equals(selectedAvatar)) {
                btn.setStyle("-fx-background-color: #2ECC71; -fx-text-fill: white; -fx-cursor: hand; -fx-background-radius: 5; -fx-border-color: #27AE60; -fx-border-width: 2; -fx-border-radius: 5;");
            } else {
                btn.setStyle("-fx-background-color: #34495E; -fx-text-fill: #BDC3C7; -fx-cursor: hand; -fx-background-radius: 5;");
            }
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
