package GUI;

import DEVICE.DiskDevice;
import FS.FileSystem;
import MEMORY.MemoryManager;
import MEMORY.RAM;
import OS.OSKernel;
import PROCES.BlockedQueue;
import javafx.application.Application;
import javafx.stage.Stage;



public class FinaliTest extends Application {

    private static OSKernel kernel;
    private static FileSystem fs;

    public static void main(String[] args) {

        RAM ram = new RAM(1024);
        MemoryManager mm = new MemoryManager(ram);
        BlockedQueue blockedQueue = new BlockedQueue(new java.util.ArrayList<>());
        DiskDevice hdd = new DiskDevice("HDD", blockedQueue);
        fs = new FileSystem(hdd);
        kernel = new OSKernel(mm, fs);

        System.out.println("--- [SISTEM] INICIJALIZACIJA U TOKU ---");
        kernel.boot();

        fs.createDirectory("/korisnik");
        System.out.println("--- [SISTEM] SVE KOMPONENTE SU SPREMNE ---");

        Thread kernelThread = new Thread(kernel);
        kernelThread.setDaemon(true);
        kernelThread.start();

        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        MainScreenGUI gui = new MainScreenGUI(kernel, fs);
        gui.start(primaryStage);
    }
}