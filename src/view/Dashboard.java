package view;

import controller.DashboardContoller;
import model.Task;
import util.SessionManger;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

public class Dashboard extends Application {
    private final DashboardContoller dashboardController = new DashboardContoller();
    public static Scene dashboardScene;
    
    // Live Event Logger Label
    private Label eventLoggerLabel;
    private VBox rootLayout;

    @Override
    public void start(Stage primaryStage) {
        rootLayout = new VBox(15);
        rootLayout.setPadding(new Insets(20));
        rootLayout.setStyle("-fx-background-color: #ECF0F1;"); // Default light theme

        // ===== TOP GREETING BAR =====
        HBox header = new HBox(15);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(10));
        header.setStyle("-fx-background-color: rgba(255, 255, 255, 0.9); -fx-background-radius: 8; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 0);");
        
        String username = SessionManger.isLoggedIn() ? SessionManger.getCurrentUser().getUsername() : "Student";
        String avatar = SessionManger.isLoggedIn() ? SessionManger.getCurrentUser().getAvatar() : "👨‍💻 Developer";
        
        // Avatar Profile Badge
        Label avatarBadge = new Label(avatar);
        avatarBadge.setFont(Font.font("Arial", FontWeight.BOLD, 15));
        avatarBadge.setPadding(new Insets(6, 12, 6, 12));
        avatarBadge.setStyle("-fx-background-color: #3498DB; -fx-text-fill: white; -fx-background-radius: 20;");
        
        Label welcomeLabel = new Label("Welcome, " + username + "! 🎓");
        welcomeLabel.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        welcomeLabel.setStyle("-fx-text-fill: #2C3E50;");
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        Button logoutBtn = new Button("Logout (خروج)");
        styleButton(logoutBtn, "#E74C3C");
        logoutBtn.setOnAction(e -> {
            SessionManger.logout();
            Login login = new Login();
            login.start(primaryStage);
        });
        
        header.getChildren().addAll(avatarBadge, welcomeLabel, spacer, logoutBtn);

        // ===== STATS CARDS =====
        int workspacesCount = dashboardController.getActiveWorkspacesCount();
        int tasksCount = dashboardController.getTodayTasksCount();
        int completedCount = dashboardController.getCompletedTasksCount();

        HBox cards = new HBox(15);
        cards.setAlignment(Pos.CENTER);
        cards.getChildren().addAll(
            createCard(String.valueOf(workspacesCount), "Enrolled Courses\nالمواد المسجلة", "#3498DB"),
            createCard(String.valueOf(tasksCount), "Study Items Left\nما يجب دراسته", "#F1C40F"),
            createCard(String.valueOf(completedCount), "Study Items Done\nما تم إنجازه", "#2ECC71")
        );

        // ===== URGENT STUDY TASKS TABLE =====
        VBox tableContainer = new VBox(10);
        tableContainer.setPadding(new Insets(10));
        tableContainer.setStyle("-fx-background-color: white; -fx-background-radius: 8;");
        
        Label tableTitle = new Label("Urgent Study Tasks & Deadlines (المهام العاجلة):");
        tableTitle.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        tableTitle.setStyle("-fx-text-fill: #2C3E50;");

        TableView<Task> table = new TableView<>();
        TableColumn<Task, String> titleCol = new TableColumn<>("Task / Course Topic");
        titleCol.setCellValueFactory(new PropertyValueFactory<>("title"));
        titleCol.setPrefWidth(200);

        TableColumn<Task, String> descCol = new TableColumn<>("Details & Instructions");
        descCol.setCellValueFactory(new PropertyValueFactory<>("description"));
        descCol.setPrefWidth(500);

        table.getColumns().add(titleCol);
        table.getColumns().add(descCol);
        ObservableList<Task> urgentTasks = FXCollections.observableArrayList(dashboardController.getUrgentTasks());
        table.setItems(urgentTasks);
        table.setPrefHeight(200);
        table.setPlaceholder(new Label("No pending study items! Time to relax! ☕🎉"));
        
        tableContainer.getChildren().addAll(tableTitle, table);

        // ===== NAVIGATION ELEMENTS BAR =====
        HBox navigationBox = new HBox(12);
        navigationBox.setAlignment(Pos.CENTER);
        
        Button openWorkspaceBtn = new Button("📚 Course Portal & explorer ➜");
        styleButton(openWorkspaceBtn, "#3498DB");
        openWorkspaceBtn.setPrefHeight(40);
        openWorkspaceBtn.setOnAction(e -> {
            Workspace_editor editor = new Workspace_editor();
            editor.start(primaryStage);
        });

        Button openServicesBtn = new Button("⏱️ Project Work Sessions Tracker ➜");
        styleButton(openServicesBtn, "#2ECC71");
        openServicesBtn.setPrefHeight(40);
        openServicesBtn.setOnAction(e -> {
            WorkTracker trackerScene = new WorkTracker();
            trackerScene.start(primaryStage);
        });

        Button openCalendarBtn = new Button("📅 Study Calendar & Appointments ➜");
        styleButton(openCalendarBtn, "#E67E22");
        openCalendarBtn.setPrefHeight(40);
        openCalendarBtn.setOnAction(e -> {
            CalendarView calendarScene = new CalendarView();
            calendarScene.start(primaryStage);
        });

        navigationBox.getChildren().addAll(openWorkspaceBtn, openServicesBtn, openCalendarBtn);

        // ===== EVENT LOGGER FOOTER BAR =====
        eventLoggerLabel = new Label("Live Event Logger: Waiting for interaction... (Move mouse or press keys!)");
        eventLoggerLabel.setFont(Font.font("Courier New", FontWeight.BOLD, 11));
        eventLoggerLabel.setPadding(new Insets(6, 12, 6, 12));
        eventLoggerLabel.setStyle("-fx-background-color: #2C3E50; -fx-text-fill: #E67E22; -fx-background-radius: 5;");
        eventLoggerLabel.setPrefWidth(750);
        eventLoggerLabel.setMaxWidth(Double.MAX_VALUE);

        rootLayout.getChildren().addAll(header, cards, tableContainer, navigationBox, eventLoggerLabel);

        // Setup Scene and event handlers
        dashboardScene = new Scene(rootLayout, 800, 600);
        primaryStage.setTitle("TaskHub - Course Home & Preferences Dashboard");
        primaryStage.setScene(dashboardScene);
        primaryStage.show();

        // Registering root events for the Lab requirements:
        rootLayout.setOnMouseMoved(e -> {
            eventLoggerLabel.setText("Mouse Moved to coordinate: (X: " + (int)e.getX() + ", Y: " + (int)e.getY() + ")");
        });

        rootLayout.setOnMouseClicked(e -> {
            eventLoggerLabel.setText("Mouse Clicked at coordinate: (X: " + (int)e.getX() + ", Y: " + (int)e.getY() + ")");
        });

        dashboardScene.setOnKeyPressed(e -> {
            eventLoggerLabel.setText("Key Pressed on keyboard: [" + e.getCode().toString() + "]");
        });

        primaryStage.widthProperty().addListener((obs, oldVal, newVal) -> {
            eventLoggerLabel.setText("Stage Resized dynamically! New Width: " + newVal.intValue() + " px");
        });
        
        primaryStage.heightProperty().addListener((obs, oldVal, newVal) -> {
            eventLoggerLabel.setText("Stage Resized dynamically! New Height: " + newVal.intValue() + " px");
        });
    }

    private VBox createCard(String number, String title, String color) {
        VBox card = new VBox(8);
        card.setAlignment(Pos.CENTER);
        card.setPrefWidth(220);
        card.setPrefHeight(100);
        card.setStyle("-fx-background-color: white; -fx-padding: 10 15 10 15; -fx-background-radius: 8; -fx-border-color: " + color + "; -fx-border-width: 2; -fx-border-radius: 8; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.05), 8, 0, 0, 0);");

        Label numLabel = new Label(number);
        numLabel.setFont(Font.font("Arial", FontWeight.BOLD, 30));
        numLabel.setStyle("-fx-text-fill: " + color + ";");

        Label titleLabel = new Label(title);
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 11));
        titleLabel.setStyle("-fx-text-fill: #7F8C8D; -fx-text-alignment: center;");

        card.getChildren().addAll(numLabel, titleLabel);
        return card;
    }

    private void styleButton(Button btn, String color) {
        btn.setStyle("-fx-background-color: " + color + "; -fx-text-fill: white; -fx-font-size: 12; -fx-cursor: hand; -fx-background-radius: 5; -fx-font-weight: bold;");
        btn.setOnMouseEntered(e -> btn.setStyle("-fx-background-color: " + darkenColor(color) + "; -fx-text-fill: white; -fx-font-size: 12; -fx-cursor: hand; -fx-background-radius: 5; -fx-font-weight: bold;"));
        btn.setOnMouseExited(e -> btn.setStyle("-fx-background-color: " + color + "; -fx-text-fill: white; -fx-font-size: 12; -fx-cursor: hand; -fx-background-radius: 5; -fx-font-weight: bold;"));
    }

    private String darkenColor(String color) {
        if (color.equals("#3498DB")) return "#2980B9";
        if (color.equals("#E74C3C")) return "#C0392B";
        if (color.equals("#2ECC71")) return "#27AE60";
        if (color.equals("#E67E22")) return "#D35400";
        return color;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
