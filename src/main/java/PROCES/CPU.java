package PROCES;

public class CPU {

    private PCB current;
    private long cycleCount;

    public CPU(){
        this.current = null;
        this.cycleCount = 0;
    }

    public void executeOneStep(){
        cycleCount++;
    }

    public void contextSwitch(PCB next){
        this.current = next;
    }

    public PCB getCurrent(){
        return current;
    }
}
