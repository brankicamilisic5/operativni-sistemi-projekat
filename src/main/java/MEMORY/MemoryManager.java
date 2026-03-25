package MEMORY;

import PROCES.PCB;

import java.util.ArrayList;
import java.util.List;

public class MemoryManager {
    private RAM ram;
    private List<MemorySegment> segments;

    public MemoryManager(RAM ram) {
        this.ram = ram;
        this.segments = new ArrayList<>();
        segments.add(new MemorySegment(null, 0, ram.size()));
    }



    private int nextPowerOfTwo(int n) {
        int p = 1;
        while (p < n) p *= 2;
        return p;
    }

    private MemorySegment findFreeSegment(int size) {
        for (MemorySegment s : segments)
            if (s.isFree() && s.getLimit() >= size) return s;
        return null;
    }

    private MemorySegment split(MemorySegment s) {
        int half = s.getLimit() / 2;
        MemorySegment left = new MemorySegment(null, s.getBase(), half);
        MemorySegment right = new MemorySegment(null, s.getBase() + half, half);

        segments.remove(s);
        segments.add(left);
        segments.add(right);
        segments.sort((a, b) -> Integer.compare(a.getBase(), b.getBase()));

        System.out.println("Split: " + s.getLimit() + " -> " + left.getLimit() + " + " + right.getLimit());
        return left;
    }

    public boolean allocate(PCB p, int size) {
        int need = nextPowerOfTwo(size);
        while (true) {
            MemorySegment s = findFreeSegment(need);
            if (s == null) return false;

            if (s.getLimit() == need) {
                s.setOwner(p);
                System.out.println("Allocated: PID=" + p.getPid() + ", Base=" + s.getBase() + ", Limit=" + s.getLimit());
                return true;
            } else {
                s = split(s);
            }
        }
    }

    public void free(PCB p) {
        for (int i = 0; i < segments.size(); i++) {
            MemorySegment s = segments.get(i);
            if (!s.isFree() && s.getOwner().equals(p)) {
                s.setOwner(null);
                System.out.println("Free: PID=" + p.getPid() + ", Base=" + s.getBase());
                merge(i);
                return;
            }
        }
    }

    private void merge(int index) {
        MemorySegment s = segments.get(index);
        MemorySegment buddy = findBuddy(s);
        if (buddy != null && buddy.isFree()) {
            int newBase = Math.min(s.getBase(), buddy.getBase());
            int newLimit = s.getLimit() * 2;

            segments.remove(s);
            segments.remove(buddy);

            MemorySegment merged = new MemorySegment(null, newBase, newLimit);
            segments.add(merged);
            segments.sort((a, b) -> Integer.compare(a.getBase(), b.getBase()));

            System.out.println("Merged do veličine: " + newLimit);
            merge(segments.indexOf(merged));
        }
    }

    private MemorySegment findBuddy(MemorySegment s) {
        int buddyBase = s.getBase() ^ s.getLimit();
        for (MemorySegment o : segments)
            if (o.getBase() == buddyBase && o.getLimit() == s.getLimit())
                return o;
        return null;
    }

    public int read(PCB p, int logicalAddress) {
        for (MemorySegment s : segments)
            if (s.getOwner() != null && s.getOwner().equals(p))
                if (logicalAddress >= 0 && logicalAddress < s.getLimit()) {
                    int phy = s.getBase() + logicalAddress;
                    System.out.println("Read: PID=" + p.getPid() + ", logAddr=" + logicalAddress + ", phyAddr=" + phy);
                    return ram.read(phy);
                }
        throw new RuntimeException("Segmentation Fault: logička adresa " + logicalAddress);
    }

    public void write(PCB p, int logicalAddress, int value) {
        for (MemorySegment s : segments)
            if (s.getOwner() != null && s.getOwner().equals(p))
                if (logicalAddress >= 0 && logicalAddress < s.getLimit()) {
                    int phy = s.getBase() + logicalAddress;
                    ram.write(phy, value);
                    System.out.println("Write: PID=" + p.getPid() + ", logAddr=" + logicalAddress + ", phyAddr=" + phy + ", value=" + value);
                    return;
                }
        throw new RuntimeException("Segmentation Fault: logička adresa " + logicalAddress);
    }

    public String dumpMemory() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < ram.size(); i++) sb.append(ram.read(i)).append(" ");
        return sb.toString().trim();
    }
}
