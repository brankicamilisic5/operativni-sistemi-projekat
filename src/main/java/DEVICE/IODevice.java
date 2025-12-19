package DEVICE;

import IO.IOOperation;
import PROCES.BlockedQueue;
import PROCES.PCB;

public abstract class IODevice {

    protected String name;
    protected  boolean busy = false;
    protected BlockedQueue queue;


    public IODevice(String name, BlockedQueue queue){
        this.name = name;
        this.queue = queue;
    }

    public abstract void startOperation(IOOperation op, PCB p);

    public boolean isBusy(){
        return busy;
    }

    public String getName(){
        return name;
    }

}
