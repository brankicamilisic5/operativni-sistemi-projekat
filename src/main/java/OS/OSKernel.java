package OS;
import FS.FileSystem;
import MEMORY.MemoryManager;
import PROCES.*;
import SYSCALL.Syscall;

import java.util.ArrayList;
import java.util.List;

public class OSKernel {
    private List<PCB> processTable;
    private ReadyQueue readyQueue;
    private BlockedQueue blockedQueue;
    private CPU cpu;
    private Scheduler scheduler;
    private MemoryManager memoryManager;
    private FileSystem fileSystem;
    //private IOManager ioManager;
    private int nextPid;

    public OSKernel(MemoryManager memoryManager, FileSystem fileSystem) {
        this.processTable = new ArrayList<>();
        this.readyQueue = new ReadyQueue(new java.util.LinkedList<>());
        this.blockedQueue = new BlockedQueue(new ArrayList<PCB>());
        this.cpu = new CPU();
        this.nextPid = 1;
        this.scheduler = new XScheduler(5);
        this.memoryManager = memoryManager;
        this.fileSystem = fileSystem;
    }

    public List<PCB> getProcessTable() {
        return processTable;
    }

    public ReadyQueue getReadyQueue() {
        return readyQueue;
    }

    public void boot() {
        System.out.println("Sistem se podiže...");
        fileSystem.createDirectory("Sistem");
        FS.File program = fileSystem.createFile("autoexec.asm");
        program.write("LOAD 10\nADD 20\nHALT");

        // Kreiramo jedan sistemski proces koji će stalno biti tu, limit 0 (on ne troši RAM)
        createProcess("SystemMonitor", 10);

        System.out.println("Sistem spreman. HDD podaci učitani u FileSystem.");
    }

    public int createProcess(String programName, int priority) {
        PCB newPcb = new PCB(nextPid++, priority, 0, 100);
        newPcb.setState(ProcessState.READY);

        processTable.add(newPcb);
        readyQueue.add(newPcb);

        return newPcb.getPid();
    }



    public void run() {
        while (!readyQueue.isEmpty()) {
            PCB next = scheduler.chooseNext(readyQueue);
            if (next == null) continue;

            System.out.println("CPU preuzima proces ID: " + next.getPid());

            cpu.contextSwitch(next);
            next.setState(ProcessState.RUNNING);

            cpu.executeOneStep(this.memoryManager);

            if (next.getState() == ProcessState.TERMINATED) {
                memoryManager.free(next);
                processTable.remove(next);
                System.out.println("Proces " + next.getPid() + " je ZAVRŠEN i memorija je oslobođena.");
            } else if (next.getState() == ProcessState.WAITING) {
                blockedQueue.block(next);
                System.out.println("Proces " + next.getPid() + " je BLOKIRAN (čeka I/O).");
            } else {
                next.setState(ProcessState.READY);
                readyQueue.add(next);
                System.out.println("Proces " + next.getPid() + " se vraća u ReadyQueue.");
            }
        }
        System.out.println("Nema više procesa! Sistem se gasi.");
    }

    public void handleSyscall(PCB p, Syscall syscall) {
        switch(syscall.getType()) {
            case READ:
                // dovrsiti (Tačka 10 i 11)
                break;
            case WRITE:
                System.out.println("[KONZOLA] Ispis (PID " + p.getPid() + "): " + syscall.getArgs());
                break;
            case EXIT:
                p.setState(ProcessState.TERMINATED);
                System.out.println("[KERNEL] Proces [PID: " + p.getPid() + "] je zatražio završetak rada.");
                break;
        }
    }

}
