package view;

import database.DatabaseConnection;
import util.SessionManger;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

public class CalendarView extends Application {
    private final ObservableList<AppointmentItem> appointmentsList = FXCollections.observableArrayList();
    private ListView<AppointmentItem> appointmentsListView;

    private TextField titleInput;
    private TextField dateTimeInput;
    private TextField subjectInput;
    private TextArea descInput;

    private AppointmentItem selectedAppointment = null;
    private Button addSubmitBtn;
    private Button deleteBtn;
    private Button updateBtn;

    // Helper class to encapsulate appointment records
    public static class AppointmentItem {
        private final int id;
        private final String title;
        private final String dateTime;
        private final String subject;
        private final String description;

        public AppointmentItem(int id, String title, String dateTime, String subject, String description) {
            this.id = id;
            this.title = title;
            this.dateTime = dateTime;
            this.subject = subject;
            this.description = description;
        }

        public int getId() { return id; }
        public String getTitle() { return title; }
        public String getDateTime() { return dateTime; }
        public String getSubject() { return subject; }
        public String getDescription() { return description; }

        @Override
        public String toString() {
            return "📅 [" + dateTime + "] " + title + " (Course: " + subject + ")\n   - " + description;
        }
    }

    @Override
    public void start(Stage primaryStage) {
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(15));
        root.setStyle("-fx-background-color: #ECF0F1;");

        // ===== TOP BAR =====
        HBox topBar = new HBox(15);
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setPadding(new Insets(10));
        topBar.setStyle("-fx-background-color: #2C3E50; -fx-background-radius: 8;");

        Button backBtn = new Button("◀ Back to Dashboard (الرئيسية)");
        styleButton(backBtn, "#3498DB");
        backBtn.setOnAction(e -> {
            Dashboard db = new Dashboard();
            db.start(primaryStage);
        });

        Label titleLabel = new Label("Student Calendar & Academic Appointments");
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        titleLabel.setStyle("-fx-text-fill: white;");

        topBar.getChildren().addAll(backBtn, titleLabel);
        root.setTop(topBar);

        // ===== SPLIT PANE CONTENT =====
        SplitPane splitPane = new SplitPane();
        splitPane.setStyle("-fx-background-color: transparent; -fx-padding: 10 0 0 0;");
        splitPane.setDividerPositions(0.42); // 42% left, 58% right

        // LEFT PANEL: SCHEDULE APPOINTMENT FORM (CREATE / UPDATE)
        VBox formContainer = new VBox(12);
        formContainer.setPadding(new Insets(15));
        formContainer.setStyle("-fx-background-color: white; -fx-background-radius: 8;");

        Label formTitle = new Label("Schedule Study Appointment (حجز موعد جديد)");
        formTitle.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        formTitle.setStyle("-fx-text-fill: #E67E22;");

        GridPane grid = new GridPane();
        grid.setHgap(8);
        grid.setVgap(10);

        titleInput = new TextField();
        titleInput.setPromptText("e.g. Study Group meeting");
        titleInput.setPrefWidth(200);

        dateTimeInput = new TextField();
        dateTimeInput.setPromptText("e.g. 2026-06-03 14:00 or Mondays at 10 AM");

        subjectInput = new TextField();
        subjectInput.setPromptText("e.g. Advanced Java OOP");

        descInput = new TextArea();
        descInput.setPromptText("Enter session description or room location...");
        descInput.setPrefHeight(80);
        descInput.setWrapText(true);

        grid.add(new Label("Session Title:"), 0, 0);
        grid.add(titleInput, 1, 0);
        grid.add(new Label("Date / Time:"), 0, 1);
        grid.add(dateTimeInput, 1, 1);
        grid.add(new Label("Subject/Course:"), 0, 2);
        grid.add(subjectInput, 1, 2);
        grid.add(new Label("Description:"), 0, 3);
        grid.add(descInput, 1, 3);

        HBox formActions = new HBox(8);
        formActions.setAlignment(Pos.CENTER);

        addSubmitBtn = new Button("Schedule Session");
        styleButton(addSubmitBtn, "#E67E22");
        
        updateBtn = new Button("Save Edits");
        styleButton(updateBtn, "#3498DB");
        updateBtn.setDisable(true);

        Button clearBtn = new Button("Clear Form");
        styleButton(clearBtn, "#7F8C8D");

        formActions.getChildren().addAll(addSubmitBtn, updateBtn, clearBtn);
        formContainer.getChildren().addAll(formTitle, grid, formActions);

        // RIGHT PANEL: ACTIVE APPOINTMENTS LIST (READ / DELETE)
        VBox listContainer = new VBox(12);
        listContainer.setPadding(new Insets(15));
        listContainer.setStyle("-fx-background-color: white; -fx-background-radius: 8;");

        Label listTitle = new Label("Your Upcoming Study Appointments (مواعيدي الدراسية القادمة)");
        listTitle.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        listTitle.setStyle("-fx-text-fill: #2C3E50;");

        appointmentsListView = new ListView<>(appointmentsList);
        appointmentsListView.setPrefHeight(380);
        appointmentsListView.setPlaceholder(new Label("No study appointments scheduled! Plan ahead! 📅🎉"));

        HBox listActions = new HBox(10);
        listActions.setAlignment(Pos.CENTER_RIGHT);

        deleteBtn = new Button("❌ Cancel Selected Appointment");
        styleButton(deleteBtn, "#E74C3C");
        deleteBtn.setDisable(true);

        listActions.getChildren().addAll(deleteBtn);
        listContainer.getChildren().addAll(listTitle, appointmentsListView, listActions);

        splitPane.getItems().addAll(formContainer, listContainer);
        root.setCenter(splitPane);

        // ===== WIRING DATABASE CRUD EVENT HANDLERS =====

        // Load data initially
        loadAppointmentsFromDB();

        // List Selection listener for UPDATE / DELETE
        appointmentsListView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                selectedAppointment = newVal;
                titleInput.setText(newVal.getTitle());
                dateTimeInput.setText(newVal.getDateTime());
                subjectInput.setText(newVal.getSubject());
                descInput.setText(newVal.getDescription());

                addSubmitBtn.setDisable(true);
                updateBtn.setDisable(false);
                deleteBtn.setDisable(false);
            } else {
                selectedAppointment = null;
                addSubmitBtn.setDisable(false);
                updateBtn.setDisable(true);
                deleteBtn.setDisable(true);
            }
        });

        // CREATE Action
        addSubmitBtn.setOnAction(e -> {
            String t = titleInput.getText();
            String dt = dateTimeInput.getText();
            String s = subjectInput.getText();
            String d = descInput.getText();

            if (t.trim().isEmpty() || dt.trim().isEmpty()) {
                Alert alert = new Alert(Alert.AlertType.WARNING, "Title and Date/Time are required fields!");
                alert.show();
                return;
            }

            if (!SessionManger.isLoggedIn()) {
                Alert alert = new Alert(Alert.AlertType.WARNING, "Please log in first to manage calendar.");
                alert.show();
                return;
            }

            boolean success = insertAppointmentInDB(t.trim(), dt.trim(), s.trim(), d.trim(), SessionManger.getCurrentUser().getId());
            if (success) {
                clearFormFields();
                loadAppointmentsFromDB();
                Alert alert = new Alert(Alert.AlertType.INFORMATION, "Academic Appointment scheduled successfully! 📅");
                alert.show();
            } else {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Failed to write appointment. Check database.");
                alert.show();
            }
        });

        // UPDATE Action
        updateBtn.setOnAction(e -> {
            if (selectedAppointment == null) return;

            String t = titleInput.getText();
            String dt = dateTimeInput.getText();
            String s = subjectInput.getText();
            String d = descInput.getText();

            if (t.trim().isEmpty() || dt.trim().isEmpty()) {
                Alert alert = new Alert(Alert.AlertType.WARNING, "Title and Date/Time cannot be blank!");
                alert.show();
                return;
            }

            boolean success = updateAppointmentInDB(selectedAppointment.getId(), t.trim(), dt.trim(), s.trim(), d.trim());
            if (success) {
                clearFormFields();
                loadAppointmentsFromDB();
                Alert alert = new Alert(Alert.AlertType.INFORMATION, "Appointment details updated successfully!");
                alert.show();
            } else {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Failed to update record. Check connection.");
                alert.show();
            }
        });

        // DELETE Action
        deleteBtn.setOnAction(e -> {
            if (selectedAppointment == null) return;

            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Are you sure you want to cancel the appointment: '" + selectedAppointment.getTitle() + "'?");
            confirm.showAndWait().ifPresent(btn -> {
                if (btn == ButtonType.OK) {
                    boolean success = deleteAppointmentFromDB(selectedAppointment.getId());
                    if (success) {
                        clearFormFields();
                        loadAppointmentsFromDB();
                        Alert alert = new Alert(Alert.AlertType.INFORMATION, "Appointment cancelled successfully.");
                        alert.show();
                    } else {
                        Alert alert = new Alert(Alert.AlertType.ERROR, "Failed to delete record. Check database.");
                        alert.show();
                    }
                }
            });
        });

        // Clear Action
        clearBtn.setOnAction(e -> clearFormFields());

        Scene scene = new Scene(root, 850, 620);
        primaryStage.setTitle("TaskHub - Study Appointments Calendar");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void clearFormFields() {
        titleInput.clear();
        dateTimeInput.clear();
        subjectInput.clear();
        descInput.clear();
        appointmentsListView.getSelectionModel().clearSelection();
        selectedAppointment = null;

        addSubmitBtn.setDisable(false);
        updateBtn.setDisable(true);
        deleteBtn.setDisable(true);
    }

    // ===== DATABASE JDBC operations =====

    private void loadAppointmentsFromDB() {
        appointmentsList.clear();
        if (!SessionManger.isLoggedIn()) return;

        String sql = "SELECT * FROM appointments WHERE user_id = ? ORDER BY date_time ASC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, SessionManger.getCurrentUser().getId());
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    appointmentsList.add(new AppointmentItem(
                        rs.getInt("id"),
                        rs.getString("title"),
                        rs.getString("date_time"),
                        rs.getString("subject"),
                        rs.getString("description")
                    ));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private boolean insertAppointmentInDB(String title, String dateTime, String subject, String desc, int userId) {
        String sql = "INSERT INTO appointments (title, date_time, subject, description, user_id) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, title);
            pstmt.setString(2, dateTime);
            pstmt.setString(3, subject);
            pstmt.setString(4, desc);
            pstmt.setInt(5, userId);

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private boolean updateAppointmentInDB(int id, String title, String dateTime, String subject, String desc) {
        String sql = "UPDATE appointments SET title = ?, date_time = ?, subject = ?, description = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, title);
            pstmt.setString(2, dateTime);
            pstmt.setString(3, subject);
            pstmt.setString(4, desc);
            pstmt.setInt(5, id);

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private boolean deleteAppointmentFromDB(int id) {
        String sql = "DELETE FROM appointments WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private void styleButton(Button btn, String color) {
        btn.setStyle("-fx-background-color: " + color + "; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand; -fx-background-radius: 5;");
        btn.setOnMouseEntered(e -> btn.setStyle("-fx-background-color: " + darkenColor(color) + "; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand; -fx-background-radius: 5;"));
        btn.setOnMouseExited(e -> btn.setStyle("-fx-background-color: " + color + "; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand; -fx-background-radius: 5;"));
    }

    private String darkenColor(String color) {
        if (color.equals("#E67E22")) return "#D35400";
        if (color.equals("#3498DB")) return "#2980B9";
        if (color.equals("#7F8C8D")) return "#6C7A89";
        if (color.equals("#E74C3C")) return "#C0392B";
        return color;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
