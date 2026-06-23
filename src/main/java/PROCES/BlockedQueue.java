package PROCES;

import DEVICE.IODevice;
import java.util.ArrayList;
import java.util.List;

public class BlockedQueue {

    private List<PCB> list;

    public BlockedQueue(List<PCB> list) {
        this.list = list;
    }

    public void block(PCB p) {
        if (!list.contains(p)) {
            p.setState(ProcessState.WAITING);
            list.add(p);
        }
    }

    public void unblock(PCB p) {
        if (list.remove(p)) {
            p.setState(ProcessState.READY);
        }
    }

    public List<PCB> getList() {
        return list;
    }

    public void remove(PCB p) {
        list.remove(p);
    }

}
