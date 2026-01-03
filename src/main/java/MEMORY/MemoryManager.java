package MEMORY;

import PROCES.PCB;

import java.util.ArrayList;
import java.util.List;

public class MemoryManager {
    private RAM ram;
    private List<MemorySegment> segments;

    public MemoryManager(RAM ram){
        this.ram=ram;
        this.segments=new ArrayList<>();
    }

    public boolean allocate(PCB p, int size) {
        int currentBase = 0;

        segments.sort((s1, s2) -> Integer.compare(s1.getBase(), s2.getBase()));

        for (MemorySegment s : segments) {
            if (s.getBase() - currentBase >= size) {
                segments.add(new MemorySegment(p, currentBase, size));
                return true;
            }
            currentBase = s.getBase() + s.getLimit();
        }


        if (ram.size() - currentBase >= size) {
            segments.add(new MemorySegment(p, currentBase, size));
            return true;
        }
        return false;
    }



    public void free(PCB p) {
        segments.removeIf(s -> s.getOwner().equals(p));
    }

    public int read(PCB p, int address) {
        for (MemorySegment s : segments) {
            if (s.getOwner().equals(p) && s.contains(s.getBase() + address)) {
                return ram.read(s.getBase() + address);
            }
        }
        throw new RuntimeException("Segmentation Fault: Neovlašten pristup ili nevažeća adresa!");
    }

    public void write(PCB p, int address, int value) {
        for (MemorySegment s : segments) {
            if (s.getOwner().equals(p) && s.contains(s.getBase() + address)) {
                ram.write(s.getBase() + address, value);
                return;
            }
        }
        throw new RuntimeException("Segmentation Fault: Pokušaj pisanja na nevažeću adresu!");
    }

    public String dumpMemory() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < ram.size(); i++) {
            sb.append(ram.read(i)).append(" ");
        }
        return sb.toString().trim();
    }

}
