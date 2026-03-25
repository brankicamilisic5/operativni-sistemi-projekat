package FS;

import java.util.Scanner;
import OS.*;

public class Shell {
    private OSKernel kernel;
    private FileSystem fs;
    private Directory currentDirectory;
    private boolean running = true;

    public Shell(OSKernel kernel, FileSystem fs) {
        this.kernel = kernel;
        this.fs = fs;
        this.currentDirectory = (Directory) fs.resolve("/");
    }

    public void start() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("--- Dobrodošli---");

        while (running) {
            System.out.print(currentDirectory.getName() + "> ");
            String input = scanner.nextLine();
            String[] parts = input.split("\\s+");
            String command = parts[0].toLowerCase();

            switch (command) {
                case "cd":
                    if (parts.length > 1) {
                        FSNode node = fs.resolve(parts[1]);
                        if (node instanceof Directory) currentDirectory = (Directory) node;
                        else System.out.println("Direktorijum nije pronađen.");
                    }
                    break;
                case "ls":
                case "dir":
                    currentDirectory.list().forEach(node ->
                            System.out.println((node instanceof Directory ? "[DIR] " : "[FILE] ") + node.getName()));
                    break;
                case "ps":
                    System.out.println("PID\tPC\tStatus\t\tAdresa\tLimit");
                    kernel.getProcessTable().forEach(p ->
                            System.out.println(p.getPid() + "\t" + p.getProgramCounter() + "\t" +
                                    p.getState() + "\t" + p.getBaseAddress() + "\t" + p.getLimit()));
                    break;
                case "mkdir":
                    if (parts.length > 1) fs.createDirectory(currentDirectory.getName() + "/" + parts[1]);
                    break;
                case "run":
                    if (parts.length > 1) kernel.createProcess(parts[1], 1);
                    break;
                case "mem":
                    System.out.println("Zauzeće memorije:\n" + kernel.getMemoryManager().dumpMemory());
                    break;
                case "rm":
                    if (parts.length > 1) fs.delete(parts[1]);
                    break;
                case "exit":
                    running = false;
                    System.out.println("Gašenje sistema...");
                    System.exit(0);
                    break;
                default:
                    System.out.println("Nepoznata komanda.");
            }
        }
    }
}