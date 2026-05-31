package view;

import controller.WorkspaceController;
import service.TimerService;
import model.Workspace;
import model.Task;
import model.PathLink;
import util.SessionManger;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import java.io.File;
import java.io.IOException;
import java.util.Optional;

/**
 * University Study Portal & Project Explorer View.
 * Heavily upgraded to support Course (مادة), Slide (سلايد), and Folder (مجلد) management.
 * Designed to look like a high-grade student project: highly functional, simple, and well-commented.
 */
public class Workspace_editor extends Application {
    private final WorkspaceController controller = new WorkspaceController();
    private final TimerService timerService = new TimerService();

    // Observable Lists
    private final ObservableList<Workspace> courseList = FXCollections.observableArrayList();
    private final ObservableList<Task> taskList = FXCollections.observableArrayList();
    private final ObservableList<PathLink> slideFolderList = FXCollections.observableArrayList();

    // UI Elements
    private ListView<Workspace> coursesListView;
    private ListView<Task> tasksListView;
    private ListView<PathLink> slidesListView;
    
    private TextField taskTitleInput;
    private TextField taskDescInput;
    
    private TextArea fileContentArea;
    private Label fileStatusLabel;
    private File currentOpenFile = null;
    
    // Timer labels
    private Label timerLabel;
    private Button startTimerBtn;

    // Progress elements for Slides & Folders
    private Label slideProgressLabel;
    private ProgressBar slideProgressBar;

    @Override
    public void start(Stage primaryStage) {
        BorderPane mainLayout = new BorderPane();
        mainLayout.setPadding(new Insets(15));
        mainLayout.setStyle("-fx-background-color: #ECF0F1;");

        // ================= TOP BAR (Header & Pomodoro Study Timer) =================
        HBox topBar = new HBox(20);
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setPadding(new Insets(10));
        topBar.setStyle("-fx-background-color: #2C3E50; -fx-background-radius: 8;");

        Button backBtn = new Button("◀ Dashboard (الرئيسية)");
        styleButton(backBtn, "#3498DB");
        backBtn.setOnAction(e -> {
            timerService.stopTimer();
            Dashboard db = new Dashboard();
            db.start(primaryStage);
        });

        Label titleLabel = new Label("University Course Portal & Project Explorer");
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        titleLabel.setStyle("-fx-text-fill: white;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Pomodoro Timer Widget
        HBox timerWidget = new HBox(10);
        timerWidget.setAlignment(Pos.CENTER_LEFT);
        timerLabel = new Label("25:00");
        timerLabel.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        timerLabel.setStyle("-fx-text-fill: #F1C40F;");

        startTimerBtn = new Button("Start Study Timer");
        styleButton(startTimerBtn, "#2ECC71");
        startTimerBtn.setOnAction(e -> {
            if (timerService.isRunning()) {
                timerService.stopTimer();
                timerLabel.setText("25:00");
                startTimerBtn.setText("Start Study Timer");
                startTimerBtn.setStyle("-fx-background-color: #2ECC71; -fx-text-fill: white; -fx-font-weight: bold;");
            } else {
                timerService.startTimer(25, 
                    time -> timerLabel.setText(time), 
                    () -> {
                        timerLabel.setText("00:00");
                        startTimerBtn.setText("Start Study Timer");
                        Alert alert = new Alert(Alert.AlertType.INFORMATION);
                        alert.setTitle("Timer Finished");
                        alert.setHeaderText("Great job studying!");
                        alert.setContentText("Study session completed. Take a short 5-minute break!");
                        alert.show();
                    }
                );
                startTimerBtn.setText("Stop");
                startTimerBtn.setStyle("-fx-background-color: #E74C3C; -fx-text-fill: white; -fx-font-weight: bold;");
            }
        });
        timerWidget.getChildren().addAll(timerLabel, startTimerBtn);

        topBar.getChildren().addAll(backBtn, titleLabel, spacer, timerWidget);
        mainLayout.setTop(topBar);

        // ================= SPLIT PANE (LEFT: Courses & Tasks, RIGHT: Files Explorer & Editor) =================
        SplitPane splitPane = new SplitPane();
        splitPane.setOrientation(Orientation.HORIZONTAL);
        splitPane.setDividerPositions(0.42); // 42% left, 58% right
        splitPane.setStyle("-fx-background-color: transparent; -fx-padding: 10 0 0 0;");

        // ----------------- LEFT PANEL: Courses (Workspaces) & Tasks -----------------
        VBox leftPanel = new VBox(12);
        leftPanel.setPadding(new Insets(10));
        leftPanel.setStyle("-fx-background-color: white; -fx-background-radius: 8;");

        // Courses Section
        Label wsHeader = new Label("Courses & Projects - المواد والمشاريع الدراسية");
        wsHeader.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        wsHeader.setStyle("-fx-text-fill: #2C3E50;");
        
        coursesListView = new ListView<>();
        coursesListView.setPrefHeight(120);
        coursesListView.setItems(courseList);
        
        HBox courseBtns = new HBox(8);
        Button addCourseBtn = new Button("+ Add Course (إضافة مادة)");
        styleButton(addCourseBtn, "#2ECC71");
        Button deleteCourseBtn = new Button("Delete Course");
        styleButton(deleteCourseBtn, "#E74C3C");
        courseBtns.getChildren().addAll(addCourseBtn, deleteCourseBtn);

        // Tasks Section (Study Items Checklist)
        Label tasksHeader = new Label("Study Checklist / Task List (قائمة المهام)");
        tasksHeader.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        tasksHeader.setStyle("-fx-text-fill: #2C3E50;");

        tasksListView = new ListView<>();
        tasksListView.setPrefHeight(150);
        tasksListView.setItems(taskList);

        // Custom task cell renderer to show completion state beautifully
        tasksListView.setCellFactory(lv -> new ListCell<Task>() {
            @Override
            protected void updateItem(Task item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    String prefix = item.getStatus().equals("COMPLETED") ? "✔ [DONE] " : "☐ [PENDING] ";
                    setText(prefix + item.getTitle());
                    if (item.getStatus().equals("COMPLETED")) {
                        setStyle("-fx-text-fill: #7F8C8D; -fx-font-style: italic;");
                    } else {
                        setStyle("-fx-text-fill: #2C3E50; -fx-font-weight: bold;");
                    }
                }
            }
        });

        // Add task inputs
        GridPane addTaskPane = new GridPane();
        addTaskPane.setHgap(8);
        addTaskPane.setVgap(6);

        taskTitleInput = new TextField();
        taskTitleInput.setPromptText("e.g. Study Ch 1");
        taskDescInput = new TextField();
        taskDescInput.setPromptText("Description (Optional)");
        
        Button addTaskBtn = new Button("Add Task");
        styleButton(addTaskBtn, "#3498DB");
        addTaskBtn.setPrefWidth(90);

        addTaskPane.add(new Label("Task:"), 0, 0);
        addTaskPane.add(taskTitleInput, 1, 0);
        addTaskPane.add(new Label("Desc:"), 0, 1);
        addTaskPane.add(taskDescInput, 1, 1);
        
        HBox taskActions = new HBox(8);
        Button deleteTaskBtn = new Button("Delete Task");
        styleButton(deleteTaskBtn, "#E74C3C");
        Button toggleTaskBtn = new Button("Toggle Check");
        styleButton(toggleTaskBtn, "#F1C40F");
        taskActions.getChildren().addAll(addTaskBtn, toggleTaskBtn, deleteTaskBtn);

        leftPanel.getChildren().addAll(wsHeader, coursesListView, courseBtns, tasksHeader, tasksListView, addTaskPane, taskActions);

        // ----------------- RIGHT PANEL: Slides, Folder Explorer & Code Editor -----------------
        VBox rightPanel = new VBox(12);
        rightPanel.setPadding(new Insets(10));
        rightPanel.setStyle("-fx-background-color: white; -fx-background-radius: 8;");

        Label filesHeader = new Label("Project Slides & Folders - ملفات المشاريع والسلايدات والفولدرات");
        filesHeader.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        filesHeader.setStyle("-fx-text-fill: #2C3E50;");

        slidesListView = new ListView<>();
        slidesListView.setPrefHeight(110);
        slidesListView.setItems(slideFolderList);

        // Custom slide/folder cell renderer to show completion state & type icon
        slidesListView.setCellFactory(lv -> new ListCell<PathLink>() {
            @Override
            protected void updateItem(PathLink item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    File file = new File(item.getFilePath());
                    String typeIcon = file.isDirectory() ? "📁 [FOLDER] " : "📄 [FILE] ";
                    String prefix = item.getStatus().equals("COMPLETED") ? "✔ [DONE] " : "☐ [PENDING] ";
                    
                    setText(prefix + typeIcon + item.getName() + " -> (" + item.getFilePath() + ")");
                    
                    if (item.getStatus().equals("COMPLETED")) {
                        setStyle("-fx-text-fill: #27AE60; -fx-font-style: italic;");
                    } else {
                        setStyle("-fx-text-fill: #2C3E50; -fx-font-weight: bold;");
                    }
                }
            }
        });

        // Slide Progress Indicators
        HBox progressBox = new HBox(10);
        progressBox.setAlignment(Pos.CENTER_LEFT);
        slideProgressLabel = new Label("Studied Progress: 0/0 (0%)");
        slideProgressLabel.setFont(Font.font("Arial", FontWeight.BOLD, 11));
        slideProgressLabel.setStyle("-fx-text-fill: #7F8C8D;");
        
        slideProgressBar = new ProgressBar(0.0);
        slideProgressBar.setPrefWidth(150);
        slideProgressBar.setStyle("-fx-accent: #2ECC71;");
        progressBox.getChildren().addAll(slideProgressLabel, slideProgressBar);

        // Slide & Folder Action Buttons
        HBox fileBtns = new HBox(8);
        Button addSlideBtn = new Button("🔗 Add Slide / File");
        styleButton(addSlideBtn, "#3498DB");
        Button addFolderBtn = new Button("📁 Add Course Folder");
        styleButton(addFolderBtn, "#2ECC71");
        Button toggleSlideStatusBtn = new Button("✓ studied Done");
        styleButton(toggleSlideStatusBtn, "#F1C40F");
        Button deleteLinkBtn = new Button("Remove");
        styleButton(deleteLinkBtn, "#E74C3C");
        fileBtns.getChildren().addAll(addSlideBtn, addFolderBtn, toggleSlideStatusBtn, deleteLinkBtn);

        // Lab Notepad / Code Editor Title
        Label editorLabel = new Label("Course Document Reader / Editor (قراءة وتعديل ملفات المشروع)");
        editorLabel.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        editorLabel.setStyle("-fx-text-fill: #2C3E50;");

        fileContentArea = new TextArea();
        fileContentArea.setPromptText("Double click a linked file to open or view it here...");
        fileContentArea.setPrefHeight(150);
        fileContentArea.setFont(Font.font("Courier New", 12));

        // Editor Theme Chooser
        HBox editorControls = new HBox(12);
        editorControls.setAlignment(Pos.CENTER_LEFT);
        Label themeLabel = new Label("Editor Style:");
        ComboBox<String> themeChooser = new ComboBox<>();
        themeChooser.getItems().addAll("Classic Light", "Dark Navy", "Matrix Green");
        themeChooser.setValue("Classic Light");
        
        themeChooser.setOnAction(e -> {
            String theme = themeChooser.getValue();
            if (theme.equals("Dark Navy")) {
                fileContentArea.setStyle("-fx-control-inner-background: #2C3E50; -fx-text-fill: white; -fx-font-family: 'Courier New';");
            } else if (theme.equals("Matrix Green")) {
                fileContentArea.setStyle("-fx-control-inner-background: black; -fx-text-fill: #00FF00; -fx-font-family: 'Courier New';");
            } else {
                fileContentArea.setStyle("-fx-control-inner-background: white; -fx-text-fill: black; -fx-font-family: 'Courier New';");
            }
        });

        Button saveFileBtn = new Button("💾 Save Text Changes");
        styleButton(saveFileBtn, "#2ECC71");

        Button openSystemBtn = new Button("🚀 Open in Windows");
        styleButton(openSystemBtn, "#3498DB");

        editorControls.getChildren().addAll(themeLabel, themeChooser, saveFileBtn, openSystemBtn);

        // Counters Status Label
        fileStatusLabel = new Label("No active file loaded.");
        fileStatusLabel.setStyle("-fx-text-fill: #7F8C8D; -fx-font-style: italic;");
        
        fileContentArea.textProperty().addListener((obs, oldText, newText) -> {
            if (currentOpenFile != null) {
                int chars = newText.length();
                int words = newText.trim().isEmpty() ? 0 : newText.trim().split("\\s+").length;
                fileStatusLabel.setText("File: " + currentOpenFile.getName() + " | Words: " + words + " | Chars: " + chars);
            }
        });

        rightPanel.getChildren().addAll(filesHeader, slidesListView, progressBox, fileBtns, editorLabel, fileContentArea, editorControls, fileStatusLabel);

        // Add panels to split pane
        splitPane.getItems().addAll(leftPanel, rightPanel);
        mainLayout.setCenter(splitPane);

        // ================= WIRING CONTROLLERS & EVENT HANDLERS =================

        // Load courses on start
        loadCourses();

        // Course selected listener
        coursesListView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                SessionManger.setActiveWorkspace(newVal);
                loadCourseDetails(newVal);
            } else {
                taskList.clear();
                slideFolderList.clear();
                currentOpenFile = null;
                fileContentArea.clear();
                fileStatusLabel.setText("No active course loaded.");
                updateProgressIndicators();
            }
        });

        // Add Course Event
        addCourseBtn.setOnAction(e -> {
            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle("New Course");
            dialog.setHeaderText("Create a new university course / project");
            dialog.setContentText("Course Name (اسم المادة):");
            Optional<String> result = dialog.showAndWait();
            result.ifPresent(name -> {
                if (!name.trim().isEmpty()) {
                    boolean success = controller.createWorkspace(name.trim(), "University course workspace");
                    if (success) {
                        loadCourses();
                    }
                }
            });
        });

        // Delete Course Event
        deleteCourseBtn.setOnAction(e -> {
            Workspace selected = coursesListView.getSelectionModel().getSelectedItem();
            if (selected != null) {
                Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Are you sure you want to delete course '" + selected.getName() + "'?");
                confirm.showAndWait().ifPresent(response -> {
                    if (response == ButtonType.OK) {
                        controller.deleteWorkspace(selected);
                        loadCourses();
                    }
                });
            }
        });

        // Add Task Event
        addTaskBtn.setOnAction(e -> {
            Workspace active = SessionManger.getActiveWorkspace();
            String title = taskTitleInput.getText();
            String desc = taskDescInput.getText();
            if (active != null && !title.trim().isEmpty()) {
                boolean success = controller.addTask(title.trim(), desc.trim(), active);
                if (success) {
                    taskTitleInput.clear();
                    taskDescInput.clear();
                    loadCourseDetails(active);
                }
            } else {
                Alert alert = new Alert(Alert.AlertType.WARNING, "Please select a course and enter a task title!");
                alert.show();
            }
        });

        // Toggle Task status event (Clicking or Toggle Check button)
        toggleTaskBtn.setOnAction(e -> {
            Task selectedTask = tasksListView.getSelectionModel().getSelectedItem();
            Workspace active = SessionManger.getActiveWorkspace();
            if (selectedTask != null && active != null) {
                controller.toggleTaskStatus(selectedTask);
                loadCourseDetails(active);
            }
        });

        tasksListView.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                Task selectedTask = tasksListView.getSelectionModel().getSelectedItem();
                Workspace active = SessionManger.getActiveWorkspace();
                if (selectedTask != null && active != null) {
                    controller.toggleTaskStatus(selectedTask);
                    loadCourseDetails(active);
                }
            }
        });

        // Delete Task Event
        deleteTaskBtn.setOnAction(e -> {
            Task selected = tasksListView.getSelectionModel().getSelectedItem();
            Workspace active = SessionManger.getActiveWorkspace();
            if (selected != null && active != null) {
                controller.deleteTask(selected);
                loadCourseDetails(active);
            }
        });

        // Add Slide/File Event (Chooser)
        addSlideBtn.setOnAction(e -> {
            Workspace active = SessionManger.getActiveWorkspace();
            if (active == null) {
                Alert alert = new Alert(Alert.AlertType.WARNING, "Select a Course first!");
                alert.show();
                return;
            }
            
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Link Lecture/Slide/Code File");
            fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("All Study Documents", "*.*"),
                new FileChooser.ExtensionFilter("PDF Slides", "*.pdf"),
                new FileChooser.ExtensionFilter("PowerPoint", "*.ppt", "*.pptx"),
                new FileChooser.ExtensionFilter("Java Files", "*.java"),
                new FileChooser.ExtensionFilter("Text Files", "*.txt")
            );
            
            File selectedFile = fileChooser.showOpenDialog(primaryStage);
            if (selectedFile != null) {
                boolean success = controller.addPathLink(selectedFile.getName(), selectedFile.getAbsolutePath(), active);
                if (success) {
                    loadCourseDetails(active);
                }
            }
        });

        // Add Study Folder Event (DirectoryChooser)
        addFolderBtn.setOnAction(e -> {
            Workspace active = SessionManger.getActiveWorkspace();
            if (active == null) {
                Alert alert = new Alert(Alert.AlertType.WARNING, "Select a Course first!");
                alert.show();
                return;
            }
            
            DirectoryChooser dirChooser = new DirectoryChooser();
            dirChooser.setTitle("Link Course Slides Folder");
            
            File selectedDir = dirChooser.showDialog(primaryStage);
            if (selectedDir != null) {
                boolean success = controller.addPathLink(selectedDir.getName(), selectedDir.getAbsolutePath(), active);
                if (success) {
                    loadCourseDetails(active);
                }
            }
        });

        // Toggle studied/done status of Slide/Folder
        toggleSlideStatusBtn.setOnAction(e -> {
            PathLink selected = slidesListView.getSelectionModel().getSelectedItem();
            Workspace active = SessionManger.getActiveWorkspace();
            if (selected != null && active != null) {
                controller.togglePathLinkStatus(selected);
                loadCourseDetails(active);
            }
        });

        // Delete File/Folder Link Event
        deleteLinkBtn.setOnAction(e -> {
            PathLink selected = slidesListView.getSelectionModel().getSelectedItem();
            Workspace active = SessionManger.getActiveWorkspace();
            if (selected != null && active != null) {
                controller.deletePathLink(selected);
                loadCourseDetails(active);
            }
        });

        // Double Click on linked item (Open or explore folder!)
        slidesListView.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                PathLink selected = slidesListView.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    handleOpenSlideFolder(selected);
                }
            }
        });

        // Open in Windows button Event
        openSystemBtn.setOnAction(e -> {
            PathLink selected = slidesListView.getSelectionModel().getSelectedItem();
            if (selected != null) {
                try {
                    controller.openFileInSystem(new File(selected.getFilePath()));
                } catch (IOException ex) {
                    Alert alert = new Alert(Alert.AlertType.ERROR, "Could not open: " + ex.getMessage());
                    alert.show();
                }
            } else {
                Alert alert = new Alert(Alert.AlertType.WARNING, "Please select a slide or folder first!");
                alert.show();
            }
        });

        // Save File Changes in text editor
        saveFileBtn.setOnAction(e -> {
            if (currentOpenFile != null) {
                try {
                    controller.writeFileContent(currentOpenFile, fileContentArea.getText());
                    Alert success = new Alert(Alert.AlertType.INFORMATION, "Text changes saved successfully!");
                    success.show();
                } catch (IOException ex) {
                    Alert alert = new Alert(Alert.AlertType.ERROR, "Failed to write file: " + ex.getMessage());
                    alert.show();
                }
            } else {
                Alert alert = new Alert(Alert.AlertType.WARNING, "No file is currently open to save!");
                alert.show();
            }
        });

        Scene scene = new Scene(mainLayout, 850, 620);
        primaryStage.setTitle("TaskHub - University Course Portal");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void loadCourses() {
        courseList.clear();
        courseList.addAll(controller.getWorkspaces());
    }

    private void loadCourseDetails(Workspace ws) {
        // Load tasks
        taskList.clear();
        taskList.addAll(controller.getTasks(ws));

        // Load path links (slides and folders)
        slideFolderList.clear();
        slideFolderList.addAll(controller.getPathLinks(ws));

        // Reset file editor
        currentOpenFile = null;
        fileContentArea.clear();
        fileStatusLabel.setText("No active file loaded.");

        // Update slides/folders completion stats
        updateProgressIndicators();
    }

    private void updateProgressIndicators() {
        int total = slideFolderList.size();
        int completed = 0;
        for (PathLink pl : slideFolderList) {
            if (pl.getStatus().equals("COMPLETED")) {
                completed++;
            }
        }
        
        if (total == 0) {
            slideProgressLabel.setText("Studied Progress: 0/0 (0%)");
            slideProgressBar.setProgress(0.0);
        } else {
            double fraction = (double) completed / total;
            int percentage = (int) (fraction * 100);
            slideProgressLabel.setText("Studied Progress: " + completed + "/" + total + " (" + percentage + "%)");
            slideProgressBar.setProgress(fraction);
        }
    }

    /**
     * Handles opening slide file OR exploring project folder content in a beautiful popup list!
     */
    private void handleOpenSlideFolder(PathLink item) {
        File file = new File(item.getFilePath());
        if (!file.exists()) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Item not found at: " + item.getFilePath());
            alert.show();
            return;
        }

        if (file.isDirectory()) {
            // EXPLORE FOLDER CONTENT - SHOW POPUP ("ويبين اشي داخل ملف المشروع")
            File[] filesInside = controller.listFilesForFolder(file);
            
            Stage popup = new Stage();
            popup.setTitle("Folder Explorer: " + file.getName());
            
            VBox popupLayout = new VBox(10);
            popupLayout.setPadding(new Insets(15));
            popupLayout.setStyle("-fx-background-color: #ECF0F1;");

            Label folderTitle = new Label("Files inside directory: " + file.getName() + "/");
            folderTitle.setFont(Font.font("Arial", FontWeight.BOLD, 13));
            
            ListView<String> folderContentList = new ListView<>();
            ObservableList<String> listItems = FXCollections.observableArrayList();
            for (File f : filesInside) {
                listItems.add((f.isDirectory() ? "📁 " : "📄 ") + f.getName());
            }
            folderContentList.setItems(listItems);
            
            HBox btnPanel = new HBox(10);
            Button openBtn = new Button("Open File (تشغيل)");
            styleButton(openBtn, "#3498DB");
            Button previewTextBtn = new Button("Load into Text Editor");
            styleButton(previewTextBtn, "#2ECC71");
            btnPanel.getChildren().addAll(openBtn, previewTextBtn);

            // Double click on folder explorer list item
            folderContentList.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2) {
                    int index = folderContentList.getSelectionModel().getSelectedIndex();
                    if (index >= 0 && index < filesInside.length) {
                        File selectedFile = filesInside[index];
                        try {
                            controller.openFileInSystem(selectedFile);
                        } catch (IOException ex) {
                            Alert alert = new Alert(Alert.AlertType.ERROR, "Could not open: " + ex.getMessage());
                            alert.show();
                        }
                    }
                }
            });

            // Action triggers
            openBtn.setOnAction(evt -> {
                int index = folderContentList.getSelectionModel().getSelectedIndex();
                if (index >= 0 && index < filesInside.length) {
                    File selectedFile = filesInside[index];
                    try {
                        controller.openFileInSystem(selectedFile);
                    } catch (IOException ex) {
                        Alert alert = new Alert(Alert.AlertType.ERROR, "Could not open: " + ex.getMessage());
                        alert.show();
                    }
                }
            });

            previewTextBtn.setOnAction(evt -> {
                int index = folderContentList.getSelectionModel().getSelectedIndex();
                if (index >= 0 && index < filesInside.length) {
                    File selectedFile = filesInside[index];
                    if (selectedFile.isDirectory()) {
                        Alert alert = new Alert(Alert.AlertType.WARNING, "Cannot preview a folder inside editor!");
                        alert.show();
                        return;
                    }
                    try {
                        String text = controller.readFileContent(selectedFile);
                        fileContentArea.setText(text);
                        currentOpenFile = selectedFile;
                        int chars = text.length();
                        int words = text.trim().isEmpty() ? 0 : text.trim().split("\\s+").length;
                        fileStatusLabel.setText("File: " + selectedFile.getName() + " | Words: " + words + " | Chars: " + chars);
                        popup.close();
                    } catch (IOException ex) {
                        Alert alert = new Alert(Alert.AlertType.ERROR, "Could not read text: " + ex.getMessage());
                        alert.show();
                    }
                }
            });

            popupLayout.getChildren().addAll(folderTitle, folderContentList, btnPanel);
            Scene popupScene = new Scene(popupLayout, 450, 350);
            popup.setScene(popupScene);
            popup.show();
            
        } else {
            // LINKED FILE CLICKED
            // Check if it's plain text/code file to preview inside app
            String name = file.getName().toLowerCase();
            if (name.endsWith(".txt") || name.endsWith(".java") || name.endsWith(".py") || 
                name.endsWith(".json") || name.endsWith(".xml") || name.endsWith(".html") || name.endsWith(".css")) {
                try {
                    String content = controller.readFileContent(file);
                    fileContentArea.setText(content);
                    currentOpenFile = file;
                    int chars = content.length();
                    int words = content.trim().isEmpty() ? 0 : content.trim().split("\\s+").length;
                    fileStatusLabel.setText("File: " + file.getName() + " | Words: " + words + " | Chars: " + chars);
                } catch (IOException ex) {
                    Alert alert = new Alert(Alert.AlertType.ERROR, "Failed to read file: " + ex.getMessage());
                    alert.show();
                }
            } else {
                // Otherwise open in default PDF/PPT system application
                try {
                    controller.openFileInSystem(file);
                } catch (IOException ex) {
                    Alert alert = new Alert(Alert.AlertType.ERROR, "Could not launch file: " + ex.getMessage());
                    alert.show();
                }
            }
        }
    }

    private void styleButton(Button btn, String color) {
        btn.setStyle("-fx-background-color: " + color + "; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");
        btn.setOnMouseEntered(e -> btn.setStyle("-fx-background-color: " + darkenColor(color) + "; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;"));
        btn.setOnMouseExited(e -> btn.setStyle("-fx-background-color: " + color + "; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;"));
    }

    private String darkenColor(String color) {
        if (color.equals("#3498DB")) return "#2980B9";
        if (color.equals("#E74C3C")) return "#C0392B";
        if (color.equals("#2ECC71")) return "#27AE60";
        if (color.equals("#F1C40F")) return "#D68910";
        return color;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
