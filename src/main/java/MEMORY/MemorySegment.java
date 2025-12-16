package MEMORY;

import PROCES.PCB;

public class MemorySegment {
    private PCB owner;
    private int base;
    private int limit;

    public MemorySegment(PCB owner, int base, int limit) {
        this.owner = owner;
        this.base = base;
        this.limit = limit;
    }

    public PCB getOwner() {return owner;}
    public int getBase() {return base;}
    public int getLimit() {return limit;}

    public boolean contains(int address) {
        return address >= base && address < base + limit;
    }
}
