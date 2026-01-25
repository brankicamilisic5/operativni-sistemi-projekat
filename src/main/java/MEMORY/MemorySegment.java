package MEMORY;

import PROCES.PCB;

public class MemorySegment {
    private PCB owner;
    private int base;
    private int limit;
    private boolean free;

    public MemorySegment(PCB owner, int base, int limit) {
        this.owner = owner;
        this.base = base;
        this.limit = limit;
        this.free = (owner==null);
    }

    public PCB getOwner() {return owner;}
    public int getBase() {return base;}
    public int getLimit() {return limit;}
    public boolean isFree() {return free;}
    public void setOwner(PCB owner) {
        this.owner = owner;
        this.free = (owner == null);
    }

    public boolean contains(int address) {
        return address >= base && address < base + limit;
    }
}


