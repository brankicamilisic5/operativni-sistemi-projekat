package PROCES;

import DEVICE.IODevice;
import java.util.ArrayList;
import java.util.List;

public class BlockedQueue {

    private List<PCB> list = new ArrayList<>();

    public BlockedQueue(List<PCB> list) {
        this.list = list;
    }

    public void block(PCB p){
        list.add(p);
    }

    public void unblock(PCB p){
        list.remove(p);
    }

    public List<PCB> findByDevice(IODevice sd){
        List<PCB> result = new ArrayList<>();

        //dovrsiti

        return  result;
    }
}
