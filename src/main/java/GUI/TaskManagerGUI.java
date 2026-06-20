package GUI;
import OS.OSKernel;
import PROCES.PCB;
import PROCES.ProcessState;
import SYSCALL.Syscall;
import SYSCALL.SyscallType;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Duration;


public class TaskManagerGUI {

    private OSKernel kernel;
    private TextArea txtProcesses;
    private TextArea txtMemory;
    private Timeline refreshTimer;

    public TaskManagerGUI(OSKernel kernel) {
        this.kernel = kernel;
    }

    public void show() {
        Stage stage = new Stage();

        TabPane tabPane = new TabPane();
        tabPane.setStyle("-fx-background-color: #1e1e2e;");

        Tab tabProc = new Tab("Procesi");
        tabProc.setClosable(false);

        VBox vProc = new VBox(10);
        vProc.setPadding(new Insets(12));
        vProc.setStyle("-fx-background-color: #1e1e2e;");

        txtProcesses = new TextArea();
        txtProcesses.setEditable(false);
        txtProcesses.setPrefHeight(300);
        txtProcesses.setStyle("-fx-control-inner-background: #2a2a3e; -fx-text-fill: #e0e0ff; " +
                "-fx-font-family: monospace; -fx-font-size: 12px;");

        HBox hKill = new HBox(8);
        hKill.setAlignment(Pos.CENTER_LEFT);
        TextField txtPid = new TextField();
        txtPid.setPromptText("PID");
        txtPid.setPrefWidth(70);
        txtPid.setStyle("-fx-background-color: #2a2a3e; -fx-text-fill: white; -fx-font-family: monospace;");

        Button btnKill = new Button("Kill");
        btnKill.setStyle("-fx-background-color: #8e2a2a; -fx-text-fill: white; -fx-font-family: monospace;");
        btnKill.setOnAction(e -> {
            try {
                int pid = Integer.parseInt(txtPid.getText().trim());
                Syscall killReq = new Syscall(SyscallType.KILL,
                        java.util.Arrays.asList(String.valueOf(pid)));
                kernel.handleSyscall(killReq, null);
                txtPid.clear();
            } catch (NumberFormatException ex) {
                txtPid.setStyle("-fx-background-color: #5e2a2a; -fx-text-fill: white;");
            }
        });

        hKill.getChildren().addAll(new Label("Kill PID:") {{
            setTextFill(Color.LIGHTGRAY);
        }}, txtPid, btnKill);

        vProc.getChildren().addAll(txtProcesses, hKill);
        tabProc.setContent(vProc);

        Tab tabMem = new Tab("Memorija");
        tabMem.setClosable(false);

        VBox vMem = new VBox(10);
        vMem.setPadding(new Insets(12));
        vMem.setStyle("-fx-background-color: #1e1e2e;");

        txtMemory = new TextArea();
        txtMemory.setEditable(false);
        txtMemory.setPrefHeight(340);
        txtMemory.setWrapText(true);
        txtMemory.setStyle("-fx-control-inner-background: #111120; -fx-text-fill: #44ff88; " +
                "-fx-font-family: monospace; -fx-font-size: 11px;");

        vMem.getChildren().add(txtMemory);
        tabMem.setContent(vMem);

        tabPane.getTabs().addAll(tabProc, tabMem);

        refreshTimer = new Timeline(new KeyFrame(Duration.seconds(1), e -> refreshData()));
        refreshTimer.setCycleCount(Timeline.INDEFINITE);
        refreshTimer.play();

        stage.setOnCloseRequest(e -> refreshTimer.stop());

        refreshData();

        Scene scene = new Scene(tabPane, 560, 420);
        stage.setTitle("Task Manager");
        stage.setScene(scene);
        stage.show();
    }

    private void refreshData() {
        Platform.runLater(() -> {

            StringBuilder sb = new StringBuilder();
            sb.append(String.format("%-6s %-12s %-10s %-6s %-10s %-8s%n",
                    "PID", "TIP", "STATUS", "PC", "INSTRUKCIJA", "ADRESA"));
            sb.append("─".repeat(60)).append("\n");

            synchronized (kernel.getProcessTable()) {
                for (PCB p : kernel.getProcessTable()) {
                    if (p.getState() != ProcessState.TERMINATED) {
                        sb.append(String.format("%-6d %-12s %-10s %-6d %-10d %-8d%n",
                                p.getPid(), p.getType(), p.getState(),
                                p.getProgramCounter(), p.getExecutedInstructions(),
                                p.getBaseAddress()));
                    }
                }
            }

            sb.append("\n── ZAVRŠENI ──\n");
            synchronized (kernel.getProcessTable()) {
                for (PCB p : kernel.getProcessTable()) {
                    if (p.getState() == ProcessState.TERMINATED) {
                        sb.append(String.format("PID %-4d | %-8s | Izvršeno: %d instr.%n", p.getPid(), p.getType(),
                                p.getExecutedInstructions()));
                    }
                }
            }

            txtProcesses.setText(sb.toString());

            StringBuilder mem = new StringBuilder();
            mem.append("RAM kapacitet: ").append(kernel.getMemoryManager().getRamSize()).append("B\n\n");
            mem.append("Zauzeti blokovi:\n");

            boolean ima = false;
            synchronized (kernel.getProcessTable()) {
                for (PCB p : kernel.getProcessTable()) {
                    if (p.getState() != ProcessState.TERMINATED) {
                        mem.append(String.format("  [%04d - %04d] -> PID %d (%s)%n",
                                p.getBaseAddress(),
                                p.getBaseAddress() + p.getLimit() - 1,
                                p.getPid(), p.getType()));
                        ima = true;
                    }
                }
            }
            if (!ima) mem.append("  (sva memorija slobodna)\n");

            mem.append("\nSadržaj RAM-a:\n");
            mem.append(kernel.getMemoryManager().dumpMemory());

            txtMemory.setText(mem.toString());
        });
    }
}