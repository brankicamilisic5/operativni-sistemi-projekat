package GUI;

import FS.Directory;
import FS.FSNode;
import FS.File;
import FS.FileSystem;
import OS.OSKernel;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class MainScreenGUI extends Application {

    private OSKernel kernel;
    private FileSystem fs;
    private Directory currentDirectory;
    private ListView<String> listView;
    private Label lblCurrentDir;

    public MainScreenGUI(OSKernel kernel, FileSystem fs) {
        this.kernel = kernel;
        this.fs = fs;
        this.currentDirectory = (Directory) fs.resolve("/");
    }

    @Override
    public void start(Stage primaryStage) {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #1e1e2e;");
        root.setPadding(new Insets(10));


        lblCurrentDir = new Label("Trenutni direktorijum: /");
        lblCurrentDir.setTextFill(Color.LIGHTGRAY);
        lblCurrentDir.setStyle("-fx-font-size: 13px; -fx-font-family: monospace;");
        BorderPane.setMargin(lblCurrentDir, new Insets(0, 0, 8, 0));
        root.setTop(lblCurrentDir);


        listView = new ListView<>();
        listView.setStyle("-fx-background-color: #2a2a3e; -fx-text-fill: white;");
        listView.setPrefWidth(280);
        listView.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) handleDoubleClick();
        });
        root.setCenter(listView);


        VBox buttons = new VBox(10);
        buttons.setPadding(new Insets(0, 0, 0, 12));
        buttons.setAlignment(Pos.TOP_CENTER);

        Button btnOpen   = makeButton("Otvori fajl");
        Button btnRun    = makeButton("Pokreni fajl");
        Button btnMkdir  = makeButton("Novi Direktorijum");
        Button btnTouch  = makeButton("+ Novi fajl");
        Button btnRm     = makeButton("Obriši");
        Button btnTaskMgr = makeButton("Task Manager");

        btnOpen.setOnAction(e -> openSelected());
        btnRun.setOnAction(e -> runSelected());
        btnMkdir.setOnAction(e -> promptAndMkdir());
        btnTouch.setOnAction(e -> promptAndTouch());
        btnRm.setOnAction(e -> deleteSelected());
        btnTaskMgr.setOnAction(e -> new TaskManagerGUI(kernel).show());

        buttons.getChildren().addAll(btnOpen, btnRun, new Separator(),
                btnMkdir, btnTouch, btnRm, new Separator(), btnTaskMgr);
        root.setRight(buttons);

        refreshList();

        Scene scene = new Scene(root, 520, 420);
        primaryStage.setScene(scene);
        primaryStage.setTitle("OS Simulator");
        primaryStage.show();
    }

    private Button makeButton(String text) {
        Button b = new Button(text);
        b.setMinWidth(130);
        b.setStyle("-fx-background-color: #3a3a5e; -fx-text-fill: white; " +
                "-fx-font-family: monospace; -fx-cursor: hand;");
        b.setOnMouseEntered(e -> b.setStyle("-fx-background-color: #5555aa; -fx-text-fill: white; " +
                "-fx-font-family: monospace; -fx-cursor: hand;"));
        b.setOnMouseExited(e -> b.setStyle("-fx-background-color: #3a3a5e; -fx-text-fill: white; " +
                "-fx-font-family: monospace; -fx-cursor: hand;"));
        return b;
    }

    private void refreshList() {
        listView.getItems().clear();

        if (currentDirectory.getParent() != null) {
            listView.getItems().add("..");
        }

        for (FSNode node : currentDirectory.list()) {
            String prefix = (node instanceof Directory) ? "[DIR]  " : "[FILE] ";
            listView.getItems().add(prefix + node.getName());
        }

        lblCurrentDir.setText("Trenutni direktorijum: " + getFullPath(currentDirectory));
    }

    private String getFullPath(Directory dir) {
        if (dir.getParent() == null) return "/";
        String p = getFullPath(dir.getParent());
        return p.equals("/") ? "/" + dir.getName() : p + "/" + dir.getName();
    }

    private String selectedName() {
        String sel = listView.getSelectionModel().getSelectedItem();
        if (sel == null) return null;
        return sel.replaceFirst("^\\[DIR\\]  |^\\[FILE\\] ", "");
    }

    private void handleDoubleClick() {
        String name = selectedName();
        if (name == null) return;

        if (name.equals("..")) {
            currentDirectory = currentDirectory.getParent();
            refreshList();
            return;
        }

        FSNode node = currentDirectory.getChild(name);
        if (node instanceof Directory) {
            currentDirectory = (Directory) node;
            refreshList();
        }
    }

    private void openSelected() {
        String name = selectedName();
        if (name == null || name.equals("..")) return;
        FSNode node = currentDirectory.getChild(name);
        if (!(node instanceof File)) {
            alert("Odaberi fajl za otvaranje.");
            return;
        }
        new FileEditorGUI((File) node, kernel).show();
    }

    private void runSelected() {
        String name = selectedName();
        if (name == null || name.equals("..")) return;
        String path = getFullPath(currentDirectory) + "/" + name;
        int pid = kernel.createProcess(path, 1);
        if (pid != -1)
            info("Proces pokrenut", "PID: " + pid + " pokrenut za " + name);
        else
            alert("Nije moguće pokrenuti: " + name);
    }

    private void promptAndMkdir() {
        TextInputDialog d = new TextInputDialog();
        d.setTitle("Novi direktorijum");
        d.setHeaderText(null);
        d.setContentText("Naziv direktorijuma:");
        d.showAndWait().ifPresent(name -> {
            if (!name.isBlank()) {
                fs.createDirectory(getFullPath(currentDirectory) + "/" + name);
                refreshList();
            }
        });
    }

    private void promptAndTouch() {
        TextInputDialog d = new TextInputDialog();
        d.setTitle("Novi fajl");
        d.setHeaderText(null);
        d.setContentText("Naziv fajla (npr. program.asm):");
        d.showAndWait().ifPresent(name -> {
            if (!name.isBlank()) {
                fs.createFile(getFullPath(currentDirectory) + "/" + name);
                refreshList();
            }
        });
    }

    private void deleteSelected() {
        String name = selectedName();
        if (name == null || name.equals("..")) return;
        fs.delete(getFullPath(currentDirectory) + "/" + name);
        refreshList();
    }

    private void alert(String msg) {
        Alert a = new Alert(Alert.AlertType.WARNING, msg, ButtonType.OK);
        a.showAndWait();
    }

    private void info(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION, msg, ButtonType.OK);
        a.setTitle(title);
        a.setHeaderText(null);
        a.showAndWait();
    }
}