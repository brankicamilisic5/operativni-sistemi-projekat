package DEVICE;

import PROCES.PCB;
import PROCES.BlockedQueue;
import IO.IOOperation;

public class DiskDevice extends IODevice {

    public DiskDevice(String name, BlockedQueue queue){
        super(name, queue);
    }

    @Override
    public void startOperation(IOOperation op, PCB p){
        busy = true;
        System.out.println("DiskDevice " + name + " startuje operaciju: " + op + " za PCB: " + p.getPid());
        try{
            Thread.sleep(op.getDuration());

        } catch(InterruptedException e){
            e.printStackTrace();
        }

        queue.unblock(p);
        System.out.println("DiskDevice " + name + "zavrsio operaciju za PCB: " + p.getPid());

        busy = false;
    }

}
