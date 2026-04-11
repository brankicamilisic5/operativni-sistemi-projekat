package DEVICE;

import IO.IOOperation;
import PROCES.PCB;
import PROCES.ProcessState;
import OS.OSKernel;

import java.util.*;

public class IOManager {

    private List<IODevice> devices;
    private OSKernel kernel;

    public IOManager(OSKernel kernel) {
        this.kernel = kernel;
        devices = new ArrayList<>();
    }

    public void addDevice(IODevice device) {
        devices.add(device);
    }

    public void requestIO(PCB p, String deviceName, IOOperation op) {

        IODevice device = findDevice(deviceName);

        if (device == null) {
            System.out.println("Uređaj ne postoji!");
            return;
        }

        if (device.isBusy()) {
            System.out.println("Uređaj je zauzet -> proces ide u BLOCKED!");

            p.setState(ProcessState.WAITING);
            kernel.getBlockedQueue().block(p);

            return;
        }

        device.startOperation(op, p);

        new Thread(() -> {
            try {
                Thread.sleep(op.getDuration());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            completeIO(device, p);

        }).start();
    }

    public void completeIO(IODevice device, PCB p) {
        device.setBusy(false);

        System.out.println("IO završen za PID " + p.getPid() + " -> proces ide u READY");

        kernel.getBlockedQueue().unblock(p);
        p.setState(ProcessState.READY);
        kernel.getReadyQueue().add(p);
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