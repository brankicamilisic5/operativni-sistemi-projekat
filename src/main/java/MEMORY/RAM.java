package MEMORY;

public class RAM {
    private int size;
    private int[] cells;

    public RAM(int size){
        this.size=size;
        this.cells=new int[size];
    }

    public int read(int address) {
        if (address < 0 || address >= size) {
            throw new IllegalArgumentException("Nevažeća adresa RAM-a: " + address);
        }
        return cells[address];
    }
    public void write(int address, int value) {
        if (address < 0 || address >= size) {
            throw new IllegalArgumentException("Nevažeća adresa RAM-a: " + address);
        }
        cells[address] = value;
    }

    public int size() {
        return size;
    }

}
