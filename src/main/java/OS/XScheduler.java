package OS;



import PROCES.PCB;
import PROCES.ReadyQueue;

public class XScheduler implements Scheduler {
    private int timeQuantum;

    public XScheduler(int timeQuantum) {
        this.timeQuantum = timeQuantum;
    }

    @Override
    public PCB chooseNext(ReadyQueue readyQueue) {
        if (readyQueue.isEmpty()) return null;

        PCB next = readyQueue.removeNext();

        return next;
    }
}
