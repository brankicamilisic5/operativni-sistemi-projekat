package PROCES;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PCB {
    private int pid;
    private ProcessState state;
    private int priority;
    private int programCounter;
    private Map<String,Integer> registers;
    private int baseAddress;
    private int limit;
    private List<OpenFileHandle> openFiles;

    public PCB(int pid, int priority, int baseAddress, int limit) {
        this.pid = pid;
        this.state = ProcessState.NEW;
        this.priority = priority;
        this.programCounter = 0;
        this.registers = new HashMap<>();
        this.baseAddress = baseAddress;
        this.limit = limit;
        this.openFiles = new ArrayList<>();
    }

    public int getPid() {return pid;}
    public void setPid(int pid) {this.pid = pid;}

    public ProcessState getState() {return state;}
    public void setState(ProcessState state) {this.state = state;}

    public int getPriority() {return priority;}
    public void setPriority(int priority) {this.priority = priority;}

    public int getProgramCounter() {return programCounter;}
    public void setProgramCounter(int programCounter) {this.programCounter = programCounter;}

    public Map<String, Integer> getRegisters() {return registers;}
    public void setRegisters(Map<String, Integer> registers) {this.registers = registers;}

    public int getBaseAddress() {return baseAddress;}
    public void setBaseAddress(int baseAddress) {this.baseAddress = baseAddress;}

    public int getLimit() {return limit;}
    public void setLimit(int limit) {this.limit = limit;}

    public List<OpenFileHandle> getOpenFiles() {return openFiles;}
    public void setOpenFiles(List<OpenFileHandle> openFiles) {this.openFiles = openFiles;}

    @Override
    public String toString() {
        return "PCB{" +
                "pid=" + pid +
                ", state=" + state +
                ", priority=" + priority +
                ", programCounter=" + programCounter +
                ", registers=" + registers +
                ", baseAddress=" + baseAddress +
                ", limit=" + limit +
                ", openFiles=" + openFiles +
                '}';
    }
}
