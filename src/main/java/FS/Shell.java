package FS;

import java.util.Scanner;
import OS.*;
import java.util.Arrays;
import java.util.List;
import PROCES.*;
import SYSCALL.*;

public class Shell {
    private OSKernel kernel;
    private FileSystem fs;
    private Directory currentDirectory;
    private boolean running = true;
    private OpenFileHandle activeHandle = null;

    public Shell(OSKernel kernel, FileSystem fs) {
        this.kernel = kernel;
        this.fs = fs;
        this.currentDirectory = (Directory) fs.resolve("/");
    }


    private String getFullPath(Directory dir) {
        if (dir.getParent() == null) return "/";
        String parentPath = getFullPath(dir.getParent());
        return parentPath.equals("/") ? "/" + dir.getName() : parentPath + "/" + dir.getName();
    }

    public void start() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("--- Dobrodošli---");

        while (running) {
            System.out.print(currentDirectory.getName() + "> ");
            String input = scanner.nextLine();
            if (input.trim().isEmpty()) continue;

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
                    System.out.println("PID\tPC\tStatus\t\tAdresa\tLimit\tInstrukcija\tTip");
                    synchronized(kernel.getProcessTable()) {
                        kernel.getProcessTable().forEach(p ->
                                System.out.println(p.getPid() + "\t" + p.getProgramCounter() + "\t" +
                                        p.getState() + "\t\t" + p.getBaseAddress() + "\t" +
                                        p.getLimit() + "\t\t" + p.getExecutedInstructions()+"\t\t"+p.getType()));
                    }
                    break;
                case "mkdir":
                    if (parts.length > 1) {
                        fs.createDirectory(getFullPath(currentDirectory) + "/" + parts[1]);
                    }
                    break;

                case "run":
                    if (parts.length > 1) {
                        int pid = kernel.createProcess(parts[1], 1);
                        if (pid != -1) {
                            System.out.println("Proces " + pid + " pokrenut.");
                        }
                    }
                    break;

                case "mem":
                    System.out.println("Zauzeće memorije:\n" + kernel.getMemoryManager().dumpMemory());
                    break;

                case "rm":
                    if (parts.length > 1) fs.delete(parts[1]);
                    break;


                case "cat":
                    if (parts.length > 1) {
                        FSNode node = fs.resolve(parts[1]);
                        if (node instanceof File f) {
                            System.out.println("=== " + f.getName() + " ===");
                            String sadrzaj = f.read();
                            System.out.println(sadrzaj.isEmpty() ? "(prazan fajl)" : sadrzaj);
                        } else {
                            System.out.println("Fajl ne postoji: " + parts[1]);
                        }
                    }
                    break;


                case "touch":
                    if (parts.length > 1) {
                        String path = getFullPath(currentDirectory) + "/" + parts[1];
                        File f = fs.createFile(path);
                        if (f != null) System.out.println("Kreiran fajl: " + parts[1]);
                    }
                    break;


                case "open":
                    if (parts.length > 1) {
                        String fullPath = parts[1].startsWith("/") ?
                                parts[1] :
                                getFullPath(currentDirectory) + "/" + parts[1];
                        FSNode node = fs.resolve(fullPath);
                        if (!(node instanceof File)) {
                            System.out.println("Fajl ne postoji: " + parts[1]);
                            break;
                        }
                        File f = (File) node;
                        System.out.println("[DMA] Iniciran prenos Disk -> RAM za: " + parts[1]);
                        kernel.getDma().transfer("HDD", "RAM", f.read(), null);
                        activeHandle = new OpenFileHandle(f, 0, FileMode.WRITE);
                        System.out.println("Fajl otvoren. Koristite: write <asm kod>");
                    }
                    break;


                case "write":
                    if (activeHandle == null) {
                        System.out.println("Nema otvorenog fajla. Koristite: open <fajl>");
                        break;
                    }
                    if (parts.length < 2) {
                        System.out.println("Upotreba: write LOAD 5\\nADD 3\\nHALT");
                        break;
                    }
                    String asmCode = input.substring(input.indexOf(' ') + 1).replace("\\n", "\n");
                    activeHandle.getFile().write(asmCode);

                    Assembler asm = new Assembler();
                    List<Integer> binary = asm.translate(activeHandle.getFile());
                    System.out.println("[ASM] Masinski kod: " + binary);

                    StringBuilder binStr = new StringBuilder("[ASM] Binarno: ");
                    for (int code : binary)
                        binStr.append(String.format("%8s", Integer.toBinaryString(code)).replace(' ', '0')).append(" ");
                    System.out.println(binStr.toString().trim());

                    System.out.println("[DMA] Cuvanje na disk (RAM -> HDD)...");
                    kernel.getDma().transfer("RAM", "HDD", asmCode, null);
                    System.out.println("Upisano i sacuvano.");
                    break;


                case "close":
                    if (activeHandle != null) {
                        System.out.println("Zatvoren fajl: " + activeHandle.getFile().getName());
                        activeHandle = null;
                    } else {
                        System.out.println("Nema otvorenog fajla.");
                    }
                    break;


                case "kill":
                    if (parts.length > 1) {
                        Syscall killReq = new Syscall(SyscallType.KILL, Arrays.asList(parts[1]));
                        kernel.handleSyscall(killReq, null);
                    } else {
                        System.out.println("Upotreba: kill <pid>");
                    }
                    break;

                case "exit":
                    running = false;
                    System.out.println("Gašenje sistema...");
                    System.exit(0);
                    break;
                case "speed":
                    if (parts.length > 1) {
                        try {
                            int ms = Integer.parseInt(parts[1]);
                            kernel.setCpuBrzina(ms);
                        } catch (NumberFormatException e) {
                            System.out.println("Upotreba: speed <ms>");
                        }
                    } else {
                        System.out.println("Trenutna brzina: " + kernel.getCpuBrzina() + "ms");
                    }
                    break;
                default:
                    System.out.println("Nepoznata komanda.");
            }
        }
    }
}
