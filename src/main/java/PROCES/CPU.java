package PROCES;

import MEMORY.MemoryManager;
import OS.OSKernel;
import SYSCALL.Syscall;
import SYSCALL.SyscallType;

import java.util.ArrayList;


public class CPU {

    private PCB current;
    private long cycleCount;
    private OSKernel kernel;

    public CPU(OSKernel kernel){
        this.current = null;
        this.cycleCount = 0;
        this.kernel = kernel;
    }

    public boolean executeOneStep(MemoryManager mm) {
        if (current == null || current.getState() == ProcessState.TERMINATED) return true;

        int pc = current.getProgramCounter();
        int opcode = mm.read(current, pc);

        cycleCount++;
        current.incrementExecuted();

        if (opcode == 1) { // LOAD
            int value = mm.read(current, pc + 1);
            current.getRegisters().put("ACC", value);
            current.setProgramCounter(pc + 2);
        }
        else if (opcode == 2) { // STORE
            int addr = mm.read(current, pc + 1);
            int acc = current.getRegisters().getOrDefault("ACC", 0);
            mm.write(current, addr, acc);
            current.setProgramCounter(pc + 2);
        }
        else if (opcode == 3) { // ADD
            int value = mm.read(current, pc + 1);
            int acc = current.getRegisters().getOrDefault("ACC", 0);
            current.getRegisters().put("ACC", acc + value);
            current.setProgramCounter(pc + 2);
        }
        else if (opcode == 6) { // SUB
            int value = mm.read(current, pc + 1);
            int acc = current.getRegisters().getOrDefault("ACC", 0);
            current.getRegisters().put("ACC", acc - value);
            current.setProgramCounter(pc + 2);
        }
        else if (opcode == 10) { // MUL
            int value = mm.read(current, pc + 1);
            int acc = current.getRegisters().getOrDefault("ACC", 0);
            current.getRegisters().put("ACC", acc * value);
            current.setProgramCounter(pc + 2);
        }
        else if (opcode == 11) { // DIV
            int value = mm.read(current, pc + 1);
            int acc = current.getRegisters().getOrDefault("ACC", 0);
            if (value == 0) {
                System.out.println("[CPU ERROR] Dijeljenje nulom kod PID: " + current.getPid() + ". Gasim proces.");
                current.setState(ProcessState.TERMINATED);
                return true;
            }
            current.getRegisters().put("ACC", acc / value);
            current.setProgramCounter(pc + 2);
        }
        else if (opcode == 4) { // JMP - bezuslovan skok
            int targetAddr = mm.read(current, pc + 1);
            current.setProgramCounter(targetAddr); // Direktno na adresu
        }
        else if (opcode == 5) { // JZ - skok ako je ACC == 0
            int targetAddr = mm.read(current, pc + 1);
            int acc = current.getRegisters().getOrDefault("ACC", 0);

            if (acc == 0) {
                current.setProgramCounter(targetAddr);
            } else {
                current.setProgramCounter(pc + 2);
            }
        }
        else if (opcode == 12) { // PRINT
            int acc = current.getRegisters().getOrDefault("ACC", 0);
            System.out.println("[ISPIS - PID " + current.getPid() + "] ACC = " + acc);
            current.setProgramCounter(pc + 2);
        }
        else if (opcode == 9) { // SYSCALL
            current.setState(ProcessState.WAITING);
            current.setProgramCounter(pc + 2);

            Syscall syscall = new Syscall(SyscallType.READ, new ArrayList<>());
            kernel.handleSyscall(syscall, current);

            System.out.println("[CPU] SYSCALL za PID: " + current.getPid());
            return true;
        }
        else if (opcode == 0) { // HALT
            current.setState(ProcessState.TERMINATED);
            current.setProgramCounter(pc + 2);
            return true;
        }
        else if (opcode == 7) { // IDLE
            int remaining = mm.read(current, pc + 1);

            if (remaining <= 0) {
                current.setProgramCounter(pc + 2);
            } else {
                mm.write(current, pc + 1, remaining - 1);
            }
        }
        else {
            System.out.println("[CPU ERROR] Nepoznat opcode: " + opcode + " (PID: " + current.getPid() + ")");
            current.setState(ProcessState.TERMINATED);
            return true;
        }

        return false;
    }

    public void contextSwitch(PCB next){
        this.current = next;
    }

    public PCB getCurrent(){
        return current;
    }

    public long getCycleCount() {
        return cycleCount;
    }

    public void setCurrent(PCB current) {
        this.current = current;
    }
}