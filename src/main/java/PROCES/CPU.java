package PROCES;

import MEMORY.MemoryManager;

public class CPU {

    private PCB current;
    private long cycleCount;

    public CPU(){
        this.current = null;
        this.cycleCount = 0;
    }

    public void executeOneStep(MemoryManager mm) {
        if (current == null) return;

        int pc = current.getProgramCounter();
        int opcode = mm.read(current, pc);

        if (opcode == 1) { // LOAD
            int value = mm.read(current, pc + 1);
            current.getRegisters().put("ACC", value);
            current.setProgramCounter(pc + 2);
        }
        else if (opcode == 3) { // ADD
            int value = mm.read(current, pc + 1);
            int currentAcc = current.getRegisters().getOrDefault("ACC", 0);
            current.getRegisters().put("ACC", currentAcc + value);
            current.setProgramCounter(pc + 2);
        }
        else if (opcode == 0) { // HALT
            current.setState(ProcessState.TERMINATED);
        }

        cycleCount++;
    }

    public void contextSwitch(PCB next){
        this.current = next;
    }

    public PCB getCurrent(){
        return current;
    }

    public void setCurrent(PCB current) {
        this.current = current;
    }
}
