package OS;
import DEVICE.ConsoleDevice;
import DEVICE.DiskDevice;
import FS.Assembler;
import FS.FSNode;
import FS.File;
import FS.FileSystem;
import IO.IOOperation;
import IO.IOType;
import MEMORY.DMAController;
import MEMORY.MemoryManager;
import PROCES.*;
import SYSCALL.Syscall;
import DEVICE.IOManager;
import SYSCALL.SyscallType;

import java.util.*;


public class OSKernel {
    private List<PCB> processTable;
    private ReadyQueue readyQueue;
    private BlockedQueue blockedQueue;
    private CPU cpu;
    private Scheduler scheduler;
    private MemoryManager memoryManager;
    private FileSystem fileSystem;
    private IOManager ioManager;
    private int nextPid;
    private List<SleepingProcess> sleepQueue;
    private DMAController dma;
    private int cpuBrzina = 500;


    public OSKernel(MemoryManager memoryManager, FileSystem fileSystem) {
        this.processTable =Collections.synchronizedList(new ArrayList<>());
        this.readyQueue = new ReadyQueue(new LinkedList<>());
        this.blockedQueue = new BlockedQueue(Collections.synchronizedList(new ArrayList<PCB>()));
        this.cpu = new CPU();
        this.nextPid = 1;
        this.scheduler = new XScheduler(5);
        this.memoryManager = memoryManager;
        this.fileSystem = fileSystem;
        this.sleepQueue = Collections.synchronizedList(new ArrayList<>());
        this.ioManager = new IOManager(this);
        this.ioManager.addDevice(new ConsoleDevice("console"));
        this.ioManager.addDevice(new DiskDevice("disk", this.blockedQueue));
        dma = new DMAController(this);
    }


    public DMAController getDma() {
        return dma;
    }

    private static class SleepingProcess {
        PCB process;
        long wakeUpTime;

        SleepingProcess(PCB p, long wakeUpTime) {
            this.process = p;
            this.wakeUpTime = wakeUpTime;
        }
    }
    public List<PCB> getProcessTable() {
        return processTable;
    }

    public ReadyQueue getReadyQueue() {
        return readyQueue;
    }


    public BlockedQueue getBlockedQueue() {
        return blockedQueue;
    }

    public void boot() {
        System.out.println("Sistem se podiže...");

        String initialData = fileSystem.readFromTxt("memorija.txt");
        dma.transfer("HDD", "RAM", initialData, null);

        try { Thread.sleep(500); } catch (InterruptedException e) {}

        parseAndLoadMemory(initialData);

        System.out.println("Sistem spreman.");
    }

    private void parseAndLoadMemory(String data) {
        if (data == null || data.equals("DEFAULT_SYSTEM_DATA")) {

            fileSystem.createDirectory("/Sistem");
            fileSystem.createDirectory("/User");
            File f = fileSystem.createFile("/Sistem/SystemMonitor");
            if (f != null) f.write("HALT");
            createProcess("/Sistem/SystemMonitor", 10);
            return;
        }

        String[] lines = data.split("\n");

        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty() || line.startsWith("#")) continue;

            String[] parts = line.split("\\s+", 3);
            if (parts.length < 2) continue;

            String tip = parts[0].toUpperCase();
            String putanja = parts[1];

            switch (tip) {
                case "DIR":
                    fileSystem.createDirectory(putanja);
                    break;

                case "FILE":
                    File f = fileSystem.createFile(putanja);
                    if (f != null && parts.length == 3) {
                        f.write(parts[2].replace("\\n", "\n"));
                    }
                    break;

                case "PROCESS":
                    createProcess(putanja, 1);
                    break;
            }
        }
    }

    public int createProcess(String programName, int priority) {
        FSNode node = fileSystem.resolve(programName);
        if (!(node instanceof File)) {
            System.out.println("Greska: Program " + programName + " nije pronadjen.");
            return -1;
        }
        File programFile = (File) node;


        Assembler assembler = new Assembler();
        List<Integer> machineCode = assembler.translate(programFile);


        PCB newPcb = new PCB(nextPid++, 1);

        boolean allocated = memoryManager.allocate(newPcb, machineCode.size());
        if (!allocated) {
            System.out.println("Greska: Nema dovoljno RAM-a (Buddy system odbio).");
            return -1;
        }


        for (int i = 0; i < machineCode.size(); i++) {
            memoryManager.write(newPcb, i, machineCode.get(i));
        }

        newPcb.setState(ProcessState.READY);
        processTable.add(newPcb);
        readyQueue.add(newPcb);

        System.out.println("Kernel: Proces " + newPcb.getPid() + " uspesno kreiran i ucitan.");
        return newPcb.getPid();
    }

    public void setCpuBrzina(int ms) {
        this.cpuBrzina = ms;
        System.out.println("[KERNEL] CPU brzina postavljena na " + ms + "ms po taktu.");
    }

    public int getCpuBrzina() {
        return cpuBrzina;
    }

    public void run() {
        int quantum = ((XScheduler)scheduler).getTimeQuantum();
        int brzina =  cpuBrzina;

        while (!readyQueue.isEmpty()) {
            wakeUpSleepingProcesses();
            PCB next = scheduler.chooseNext(readyQueue);
            if (next == null) continue;

            System.out.println("\n>>> CPU preuzima PID: " + next.getPid());
            cpu.contextSwitch(next);
            next.setState(ProcessState.RUNNING);


            for (int i = 0; i < quantum; i++) {
                boolean stop = cpu.executeOneStep(this.memoryManager);

                try { Thread.sleep(brzina); } catch (InterruptedException e) {}

                if (stop || next.getState() == ProcessState.TERMINATED || next.getState() == ProcessState.WAITING) {
                    break;
                }
            }


            if (next.getState() == ProcessState.TERMINATED) {
                memoryManager.free(next);
                processTable.remove(next);
                System.out.println("---Proces " + next.getPid() + " ZAVRŠEN i memorija oslobođena.");
            } else if (next.getState() == ProcessState.WAITING) {
                blockedQueue.block(next);
                System.out.println("---Proces " + next.getPid() + " BLOKIRAN.");
            } else {

                next.setState(ProcessState.READY);
                readyQueue.add(next);
                System.out.println("--- PID " + next.getPid() + " istakao kvant, vraćen u ReadyQueue.");
            }
        }
        System.out.println("\nNema više procesa! Sistem se gasi.");
    }

    public void handleSyscall(Syscall request, PCB p) {

            switch (request.getType()) {

                case READ:
                case WRITE:

                    IOType tip;

                    if (request.getType() == SyscallType.READ) {
                        tip = IOType.READ;
                    } else {
                        tip = IOType.WRITE;
                    }

                    IOOperation op = new IOOperation(
                            tip,
                            request.getArgs().isEmpty() ? "" : request.getArgs().get(0),
                            2000
                    );

                    p.setState(ProcessState.WAITING);
                    blockedQueue.block(p);
                    cpu.setCurrent(null);

                    long duration = 0;

                    ioManager.requestIO(p, "console", op);

                    break;

                case CREATE_PROCESS:
                    createProcess(request.getArgs().get(0), 1);
                    break;

                case EXIT:
                    terminateProcess(p);
                    break;

                case SLEEP:
                    if (request.getArgs().isEmpty()) {
                        System.out.println("Sleep syscall: nedostaje trajanje (ms)");
                        break;
                    }

                    try {
                        duration = Long.parseLong(request.getArgs().get(0));
                        p.setState(ProcessState.WAITING);
                        cpu.setCurrent(null);

                        sleepQueue.add(new SleepingProcess(p, System.currentTimeMillis() + duration));
                        System.out.println("Proces " + p.getPid() + " spava " + duration + " ms");

                    } catch (NumberFormatException e) {
                        System.out.println("Sleep syscall: neispravan argument");
                    }
                    break;

                case YIELD:
                    doYield(p);
                    break;

                case KILL:
                    handleKill(request);
                    break;

                case OPEN:
                    System.out.println("Open syscall (nije IO u ovom modelu)");
                    break;
                default:
                    throw new IllegalStateException("Unexpected value: " + request.getType());
            }
        }

    private void handleKill(Syscall syscall) {

        if (syscall.getArgs().isEmpty()) {
            System.out.println("[KERNEL] KILL nema PID argument.");
            return;
        }

        try {
            int targetPid = Integer.parseInt(syscall.getArgs().get(0));
            PCB target = null;

            for (PCB pcb : processTable) {
                if (pcb.getPid() == targetPid) {
                    target = pcb;
                    break;
                }
            }

            if (target == null) {
                System.out.println("[KERNEL] Proces sa PID "
                        + targetPid + " ne postoji.");
                return;
            }


            target.setState(ProcessState.TERMINATED);

            readyQueue.remove(target);
            blockedQueue.unblock(target);

            synchronized(sleepQueue) {
                sleepQueue.removeIf(sp -> sp.process.getPid() == targetPid);
            }

            System.out.println("[KERNEL] Proces " + targetPid + " je označen za gašenje.");

        } catch (NumberFormatException e) {
            System.out.println("[KERNEL] Neispravan PID.");
        }
    }

    public void terminateProcess(PCB p) {

        System.out.println("Gasim proces " + p.getPid());

        p.setState(ProcessState.TERMINATED);

        if (cpu.getCurrent() == p) {
            cpu.setCurrent(null);
        }

        readyQueue.remove(p);
        blockedQueue.remove(p);
        processTable.remove(p);

    }

    public void doYield(PCB p) {

        System.out.println("Proces " + p.getPid() + " yield");

        p.setState(ProcessState.READY);
        readyQueue.add(p);

        cpu.setCurrent(null);

        dispatch();
    }



    public void createProcess(List<String> args) {

        PCB novi = new PCB(nextPid++, 1);

        novi.setState(ProcessState.READY);

        readyQueue.add(novi);
        processTable.add(novi);

        System.out.println("Kreiran proces " + novi.getPid());
    }

    private PCB findProcess(int pid) {
        for (PCB p : processTable) {
            if (p.getPid() == pid) return p;
        }
        return null;
    }

    private int generatePid() {
        return nextPid++;
    }

    public void dispatch() {

        PCB next = scheduler.chooseNext(readyQueue);

        if (next != null) {
            next.setState(ProcessState.RUNNING);
            cpu.setCurrent(next);

            System.out.println("CPU izvršava proces " + next.getPid());
        }
    }

    private void wakeUpSleepingProcesses() {
        long now = System.currentTimeMillis();
        List<PCB> toWake = new ArrayList<>();

        Iterator<SleepingProcess> iter = sleepQueue.iterator();
        while (iter.hasNext()) {
            SleepingProcess sp = iter.next();
            if (sp.wakeUpTime <= now) {
                toWake.add(sp.process);
                iter.remove();
            }
        }


        for (PCB p : toWake) {
            blockedQueue.unblock(p);
            p.setState(ProcessState.READY);
            readyQueue.add(p);
            System.out.println("Proces " + p.getPid() + " se probudio iz sleep-a");
        }
    }

    public MemoryManager getMemoryManager() {
        return memoryManager;
    }
}
