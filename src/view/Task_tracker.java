package view;

import model.Task;
import javafx.scene.control.Alert;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import java.util.List;

public class Task_tracker {

    public static void showProgressPopup(String workspaceName, List<Task> tasks) {
        if (tasks == null || tasks.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION, "No tasks inside workspace '" + workspaceName + "' to track!");
            alert.show();
            return;
        }

        int total = tasks.size();
        int completed = 0;
        for (Task t : tasks) {
            if (t.getStatus().equals("COMPLETED")) {
                completed++;
            }
        }

        double fraction = (double) completed / total;
        int percentage = (int) (fraction * 100);

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Task Tracker Progress");
        alert.setHeaderText("Progress for workspace: " + workspaceName);

        VBox content = new VBox(15);
        content.setAlignment(Pos.CENTER);
        content.setPadding(new Insets(10));

        Label percentLabel = new Label(percentage + "% Completed (" + completed + "/" + total + " tasks)");
        percentLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));

        ProgressBar progressBar = new ProgressBar(fraction);
        progressBar.setPrefWidth(250);
        progressBar.setStyle("-fx-accent: #2ECC71;");

        content.getChildren().addAll(percentLabel, progressBar);

        alert.getDialogPane().setContent(content);
        alert.showAndWait();
    }
}
