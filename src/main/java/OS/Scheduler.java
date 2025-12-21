package OS;

import PROCES.PCB;
import PROCES.ReadyQueue;

public interface Scheduler {
    PCB chooseNext(ReadyQueue readyQueue);
}
