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

        String pidInfo = (p == null) ? "KERNEL" : String.valueOf(p.getPid());

        System.out.println("DiskDevice " + name + " startuje operaciju: " + op + " za: " + pidInfo);



    }


}
