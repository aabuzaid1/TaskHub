package view;

import database.DatabaseConnection;
import util.SessionManger;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import java.sql.*;
import java.time.LocalDate;

public class WorkTracker extends Application {
    private final ObservableList<String> logList = FXCollections.observableArrayList();
    private ListView<String> logListView;
    private BarChart<String, Number> barChart;

    @Override
    public void start(Stage primaryStage) {
        VBox root = new VBox(15);
        root.setPadding(new Insets(15));
        root.setStyle("-fx-background-color: #ECF0F1;");

        // Top Navigation Bar
        HBox topBar = new HBox(15);
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setStyle("-fx-background-color: #2C3E50; -fx-padding: 10; -fx-background-radius: 5;");
        
        Button backBtn = new Button("◀ Back to Dashboard (الرئيسية)");
        backBtn.setStyle("-fx-background-color: #3498DB; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");
        backBtn.setOnAction(e -> {
            Dashboard db = new Dashboard();
            db.start(primaryStage);
        });

        Label title = new Label("Project Work Tracker & Productivity Hub");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 15));
        title.setStyle("-fx-text-fill: white;");
        topBar.getChildren().addAll(backBtn, title);

        // Simple Form (Second Year style)
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(15));
        grid.setStyle("-fx-background-color: white; -fx-background-radius: 5;");

        TextField projectInput = new TextField();
        projectInput.setPromptText("e.g. TaskHub Java Project");

        TextField hoursInput = new TextField();
        hoursInput.setPromptText("e.g. 3.5");

        TextField notesInput = new TextField();
        notesInput.setPromptText("e.g. Coded the login scene UI");

        Button logBtn = new Button("Log Work Session (تسجيل ساعات العمل)");
        logBtn.setStyle("-fx-background-color: #2ECC71; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");

        grid.add(new Label("Project Name:"), 0, 0);
        grid.add(projectInput, 1, 0);
        grid.add(new Label("Hours Worked:"), 0, 1);
        grid.add(hoursInput, 1, 1);
        grid.add(new Label("What did you do?:"), 0, 2);
        grid.add(notesInput, 1, 2);
        grid.add(logBtn, 1, 3);

        // Horizontal Split: ListView on Left, BarChart on Right
        HBox split = new HBox(15);
        VBox.setVgrow(split, Priority.ALWAYS);

        logListView = new ListView<>(logList);
        logListView.setPrefWidth(320);
        logListView.setPlaceholder(new Label("No work logged yet! Start coding! 💻"));

        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setLabel("Project Name");
        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Total Hours Spent");

        barChart = new BarChart<>(xAxis, yAxis);
        barChart.setLegendVisible(false);
        barChart.setPrefWidth(450);

        split.getChildren().addAll(logListView, barChart);
        root.getChildren().addAll(topBar, grid, split);

        // Action Trigger
        logBtn.setOnAction(e -> {
            String proj = projectInput.getText();
            String hrs = hoursInput.getText();
            String notes = notesInput.getText();

            if (proj.trim().isEmpty() || hrs.trim().isEmpty()) {
                Alert alert = new Alert(Alert.AlertType.WARNING, "Fill in Project Name and Hours!");
                alert.show();
                return;
            }

            try {
                double h = Double.parseDouble(hrs);
                if (SessionManger.isLoggedIn()) {
                    saveSessionToDB(proj.trim(), h, notes.trim(), SessionManger.getCurrentUser().getId());
                    projectInput.clear();
                    hoursInput.clear();
                    notesInput.clear();
                    loadSessions();
                    loadChart();
                    Alert alert = new Alert(Alert.AlertType.INFORMATION, "Work session logged successfully! Keep it up! 🎉");
                    alert.show();
                }
            } catch (NumberFormatException ex) {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Hours must be a valid decimal number!");
                alert.show();
            }
        });

        // Initialize lists and chart
        loadSessions();
        loadChart();

        Scene scene = new Scene(root, 800, 600);
        primaryStage.setTitle("TaskHub - Project Work Tracker");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void saveSessionToDB(String proj, double hrs, String notes, int userId) {
        String sql = "INSERT INTO project_sessions (project_name, hours_spent, work_date, user_id) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, proj);
            pstmt.setDouble(2, hrs);
            pstmt.setString(3, LocalDate.now().toString() + " (" + notes + ")");
            pstmt.setInt(4, userId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadSessions() {
        logList.clear();
        if (!SessionManger.isLoggedIn()) return;

        String sql = "SELECT * FROM project_sessions WHERE user_id = ? ORDER BY id DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, SessionManger.getCurrentUser().getId());
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                logList.add("💻 " + rs.getString("project_name") + " - " + rs.getDouble("hours_spent") + " hrs on " + rs.getString("work_date"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadChart() {
        barChart.getData().clear();
        if (!SessionManger.isLoggedIn()) return;

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        String sql = "SELECT project_name, SUM(hours_spent) as total FROM project_sessions WHERE user_id = ? GROUP BY project_name";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, SessionManger.getCurrentUser().getId());
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                series.getData().add(new XYChart.Data<>(rs.getString("project_name"), rs.getDouble("total")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        barChart.getData().add(series);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
