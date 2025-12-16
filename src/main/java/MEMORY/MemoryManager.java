package MEMORY;

import PROCES.PCB;

import java.util.ArrayList;
import java.util.List;

public class MemoryManager {
    private RAM ram;
    List<MemorySegment> segments;

    public MemoryManager(RAM ram){
        this.ram=ram;
        this.segments=new ArrayList<>();
    }

    //public boolean allocate(PCB p,int size){}

    public void free(PCB p){}

    public int read(PCB p, int address){
            for (MemorySegment s : segments) {
                if (s.getOwner() == p && s.contains(address)) {
                    return ram.read(address-s.getBase());
                }
            }
            throw new RuntimeException("Nevažeća adresa ili pristup memoriji");
    }

    public void write(PCB p, int address,int value){
        for (MemorySegment s : segments) {
            if (s.getOwner()==p && s.contains(address)) {
                ram.write(address-s.getBase(),value);
                return;
            }
        }
        throw new RuntimeException("Nevažeća adresa ili pristup memoriji");

    }
    public String dumpMemory() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < ram.size(); i++) {
            sb.append(ram.read(i)).append(" ");
        }
        return sb.toString();
    }

}
