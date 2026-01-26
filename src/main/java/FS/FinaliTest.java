package FS;

import FS.*;
import MEMORY.*;
import PROCES.*;
import OS.*;
import java.util.List;

class FinalniTest {
    public static void main(String[] args) {
        RAM ram = new RAM(256);
        MemoryManager memoryManager = new MemoryManager(ram);
        FileSystem fs = new FileSystem(null);
        Assembler assembler = new Assembler();
        OSKernel kernel = new OSKernel(memoryManager, fs);

        File programFile = fs.createFile("mojProgram.asm");
        programFile.write("LOAD 10\nADD 5\nHALT");

        List<Integer> kod = assembler.translate(programFile);
        System.out.println("Prevedeni kod: " + kod);

        PCB pcb = new PCB(1, 5, 0, kod.size());

        boolean uspjelaAlokacija = memoryManager.allocate(pcb, kod.size());

        if (uspjelaAlokacija) {
            System.out.println("Neprekidna alokacija uspjela!");
            System.out.println("Baza procesa: " + pcb.getBaseAddress());

            for (int i = 0; i < kod.size(); i++) {
                memoryManager.write(pcb, i, kod.get(i));
            }

            System.out.println("Stanje RAM-a: " + memoryManager.dumpMemory());

            pcb.setState(ProcessState.READY);
            kernel.getProcessTable().add(pcb);
            kernel.getReadyQueue().add(pcb);

            System.out.println("\nPOKRETANJE IZVRŠAVANJA");
            kernel.run();

            System.out.println("\nREZULTAT");
            System.out.println("Registri procesa (ACC): " + pcb.getRegisters().get("ACC"));
            System.out.println("Stanje procesa: " + pcb.getState());
        }
    }
}