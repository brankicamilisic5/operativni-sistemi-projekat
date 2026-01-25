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
        segments.add(new MemorySegment(null,0,ram.size()));
    }

    private int nextPowerOfTwo(int n) {
        int p = 1;
        while (p < n)
            p *= 2;

        return p;
    }

    private MemorySegment findFreeSegment(int size){

        for (MemorySegment s : segments){
            if (s.isFree() && s.getLimit() >= size)
                return s;
        }
        return null;
    }

    private void split(MemorySegment s){
        int half = s.getLimit() / 2;

        MemorySegment left = new MemorySegment(null, s.getBase(), half);
        MemorySegment right = new MemorySegment(null, s.getBase() + half, half);

        segments.remove(s);
        segments.add(left);
        segments.add(right);
    }



    public boolean allocate(PCB p, int size){

        int need = nextPowerOfTwo(size);

        while(true){
            MemorySegment s = findFreeSegment(need);

            if(s == null) return false;

            if(s.getLimit() == need){
                s.setOwner(p);
                return true;
            }else{
                split(s);
            }
        }
    }


    public void free(PCB p){

        for(MemorySegment s : segments){

            if(!s.isFree() &&
                    s.getOwner().equals(p)){

                s.setOwner(null);
                merge(s);
                return;
            }
        }
    }

    private void merge(MemorySegment s) {
        MemorySegment buddy = findBuddy(s);

        if (buddy != null && buddy.isFree()) {
            int newBase = Math.min(s.getBase(), buddy.getBase());
            int newLimit = s.getLimit() * 2;

            segments.remove(s);
            segments.remove(buddy);

            MemorySegment merged = new MemorySegment(null, newBase, newLimit);
            segments.add(merged);

            merge(merged);
        }
    }




    private MemorySegment findBuddy(MemorySegment s){
        int buddyBase = s.getBase() ^ s.getLimit();

        for(MemorySegment o : segments){
            if(o.getBase() == buddyBase &&
                    o.getLimit() == s.getLimit())
                return o;
        }

        return null;
    }

    public int read(PCB p, int address) {
        for (MemorySegment s : segments) {
            if (s.getOwner().equals(p) && s.contains(address)) {
                return ram.read(s.getBase() + address);
            }
        }
        throw new RuntimeException("Segmentation Fault: Neovlašten pristup ili nevažeća adresa!");
    }

    public void write(PCB p, int address, int value) {
        for (MemorySegment s : segments) {
            if (s.getOwner().equals(p) && s.contains(address)) {
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
