package PROCES;

import DEVICE.IODevice;
import java.util.ArrayList;
import java.util.List;

public class BlockedQueue {

    private List<PCB> list = new ArrayList<>();

    public BlockedQueue(List<PCB> list) {
        this.list = list;
    }

    public BlockedQueue() {
        this.list = new ArrayList<>();
    }


    public void block(PCB p) {
        p.setState(ProcessState.WAITING);
        list.add(p);
    }


    public void unblock(PCB p) {
        if (list.remove(p)) {
            p.setState(ProcessState.READY);
        }
    }

    public void remove(PCB p) {
        list.remove(p);
    }


    public List<PCB> findByDevice(IODevice sd) {
        List<PCB> result = new ArrayList<>();
        for (PCB p : list) {

            if (p.getState() == ProcessState.WAITING) {
                result.add(p);
            }
        }
        return result;
    }




}
