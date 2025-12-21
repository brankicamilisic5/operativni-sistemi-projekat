package OS;


import FS.FileSystem;
import MEMORY.MemoryManager;
import PROCES.*;


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




    public void run() {
        while (!readyQueue.isEmpty()) {
            PCB next = scheduler.chooseNext(readyQueue);

            if (next == null) {
                continue;
            }

            cpu.setCurrent(next);
            cpu.executeOneStep();

            if (next.getState() == ProcessState.READY) {
                readyQueue.add(next);
            } else if (next.getState()==ProcessState.TERMINATED) {
                memoryManager.free(next);
                processTable.remove(next);
            }
        }
        System.out.println("Nema vise procesa!");
    }


}
