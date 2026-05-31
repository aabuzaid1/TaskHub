package view;

import controller.LoginController;
import database.DatabaseConnection;
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

public class Login extends Application {
    private final LoginController loginController = new LoginController();
    public static Stage mainStage; // Track stage to switch scenes

    @Override
    public void start(Stage primaryStage) {
        mainStage = primaryStage;
        
        // Auto-initialize database tables when the app launches!
        DatabaseConnection.initDatabase();

        // UI Components
        Label title = new Label("TaskHub");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 36));
        title.setStyle("-fx-text-fill: #3498DB;");

        Label subtitle = new Label("Student Project & Lab Organizer");
        subtitle.setFont(Font.font("Arial", 14));
        subtitle.setStyle("-fx-text-fill: #BDC3C7;");

        TextField username = new TextField();
        username.setPromptText("Username");
        username.setPrefHeight(40);
        username.setStyle("-fx-font-size: 13; -fx-background-radius: 5; -fx-padding: 10;");

        PasswordField password = new PasswordField();
        password.setPromptText("Password");
        password.setPrefHeight(40);
        password.setStyle("-fx-font-size: 13; -fx-background-radius: 5; -fx-padding: 10;");

        Button signInBtn = new Button("Sign In");
        signInBtn.setPrefWidth(120);
        signInBtn.setPrefHeight(40);
        signInBtn.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        signInBtn.setStyle("-fx-background-color: #2ECC71; -fx-text-fill: white; -fx-cursor: hand; -fx-background-radius: 5;");

        Button signUpBtn = new Button("Sign Up");
        signUpBtn.setPrefWidth(120);
        signUpBtn.setPrefHeight(40);
        signUpBtn.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        signUpBtn.setStyle("-fx-background-color: #3498DB; -fx-text-fill: white; -fx-cursor: hand; -fx-background-radius: 5;");

        // Action Handlers
        signInBtn.setOnAction(e -> {
            boolean success = loginController.handleLogin(username.getText(), password.getText());
            if (success) {
                // Switch to Dashboard
                Dashboard dashboard = new Dashboard();
                dashboard.start(primaryStage);
            }
        });

        signUpBtn.setOnAction(e -> {
            Register registerScene = new Register();
            registerScene.start(primaryStage);
        });

        // Layout
        VBox root = new VBox(20);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(50));
        root.setStyle("-fx-background-color: #2C3E50;");

        VBox inputBox = new VBox(15);
        inputBox.setPrefWidth(280);
        inputBox.setMaxWidth(280);
        inputBox.getChildren().addAll(username, password);

        HBox btnBox = new HBox(15);
        btnBox.setAlignment(Pos.CENTER);
        btnBox.getChildren().addAll(signInBtn, signUpBtn);

        root.getChildren().addAll(title, subtitle, inputBox, btnBox);

        Scene scene = new Scene(root, 600, 500);
        primaryStage.setTitle("TaskHub - Welcome");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}