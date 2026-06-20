package GUI;

import FS.Assembler;
import FS.File;
import OS.OSKernel;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;


public class FileEditorGUI {

    private File file;
    private OSKernel kernel;

    public FileEditorGUI(File file, OSKernel kernel) {
        this.file = file;
        this.kernel = kernel;
    }

    public void show() {
        Stage stage = new Stage();

        VBox root = new VBox(10);
        root.setPadding(new Insets(15));
        root.setStyle("-fx-background-color: #1e1e2e;");

        Label lblDma = new Label("[DMA] Učitavanje fajla: " + file.getName() + " (Disk -> RAM)");
        lblDma.setStyle("-fx-text-fill: #aaaaff; -fx-font-family: monospace; -fx-font-size: 11px;");

        TextArea txtEditor = new TextArea(file.read());
        txtEditor.setPrefHeight(280);
        txtEditor.setWrapText(true);
        txtEditor.setStyle("-fx-control-inner-background: #2a2a3e; -fx-text-fill: #e0e0ff; " +
                "-fx-font-family: monospace; -fx-font-size: 13px;");

        TextArea txtBinary = new TextArea();
        txtBinary.setEditable(false);
        txtBinary.setPrefHeight(80);
        txtBinary.setWrapText(true);
        txtBinary.setPromptText("Binarni zapis će se prikazati ovdje nakon prevođenja...");
        txtBinary.setStyle("-fx-control-inner-background: #111120; -fx-text-fill: #44ff88; " +
                "-fx-font-family: monospace; -fx-font-size: 11px;");

        HBox hButtons = new HBox(10);
        hButtons.setAlignment(Pos.CENTER_RIGHT);

        Button btnTranslate = new Button("Prevedi (ASM -> Binary)");
        btnTranslate.setStyle("-fx-background-color: #3a5e3a; -fx-text-fill: white; -fx-font-family: monospace;");
        btnTranslate.setOnAction(e -> {
            String asmCode = txtEditor.getText();
            file.write(asmCode);

            Assembler asm = new Assembler();
            java.util.List<Integer> binary = asm.translate(file);

            StringBuilder sb = new StringBuilder();
            for (int code : binary)
                sb.append(String.format("%8s", Integer.toBinaryString(code)).replace(' ', '0')).append(" ");
            txtBinary.setText(sb.toString().trim());
        });

        Button btnSave = new Button("Sačuvaj (RAM -> Disk)");
        btnSave.setStyle("-fx-background-color: #3a3a8e; -fx-text-fill: white; -fx-font-family: monospace;");
        btnSave.setOnAction(e -> {
            String asmCode = txtEditor.getText();
            file.write(asmCode);

            kernel.getDma().transfer("RAM", "HDD", asmCode, null);

            lblDma.setText("[DMA] Sačuvano: RAM -> Disk");
            lblDma.setStyle("-fx-text-fill: #44ff88; -fx-font-family: monospace; -fx-font-size: 11px;");
        });

        Button btnClose = new Button("Zatvori");
        btnClose.setStyle("-fx-background-color: #5e2a2a; -fx-text-fill: white; -fx-font-family: monospace;");
        btnClose.setOnAction(e -> stage.close());

        hButtons.getChildren().addAll(btnTranslate, btnSave, btnClose);

        root.getChildren().addAll(lblDma, new Label() {{
            setText(file.getName());
            setStyle("-fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold;");
        }}, txtEditor, txtBinary, hButtons);

        kernel.getDma().transfer("HDD", "RAM", file.read(), null);

        Scene scene = new Scene(root, 560, 480);
        stage.setTitle("Editor: " + file.getName());
        stage.setScene(scene);
        stage.show();
    }
}