package DEVICE;

import IO.IOOperation;
import PROCES.PCB;
import PROCES.ProcessState;

import java.util.*;

public class IOManager {

    private List<IODevice> devices;

    public IOManager() {
        devices = new ArrayList<>();
    }

    public void addDevice(IODevice device) {
        devices.add(device);
    }

    public static void requestIO(PCB p, String deviceName, IOOperation op) {

        IODevice device = findDevice(deviceName);

        if (device == null) {
            System.out.println("Uređaj ne postoji!");
            return;
        }

        if (device.isBusy()) {
            System.out.println("Uređaj je zauzet -> proces ide u BLOCKED!");
            p.setState(ProcessState.WAITING);
            return;
        }

        device.startOperation(op, p);
        completeIO(device, p);
    }

    public void completeIO(IODevice device, PCB p) {
        System.out.println("IO završen -> proces ide u READY");

        p.setState(ProcessState.READY);
    }

    private IODevice findDevice(String name) {
        for (IODevice d : devices) {
            if (d.getName().equals(name)) {
                return d;
            }
        }
        return null;
    }
}
