package DEVICE;

import PROCES.PCB;
import PROCES.BlockedQueue;
import IO.IOOperation;

import java.util.*;

public class DiskDevice extends IODevice {
    private int currentHeadPosition = 0;
    private boolean movingUp = true;

    public DiskDevice(String name, BlockedQueue queue){
        super(name, queue);
    }

    @Override
    public void startOperation(IOOperation op, PCB p){
        busy = true;

        int requestedBlock = (int)(Math.random() * 128);

        List<Integer> requests = new ArrayList<>();
        requests.add(requestedBlock);
        requests.add((int)(Math.random() * 128));
        requests.add((int)(Math.random() * 128));


        if (movingUp) {
            Collections.sort(requests);
        } else {
            requests.sort(Collections.reverseOrder());
        }

        System.out.println("\n[SCAN DISK] Glava kreće sa: " + currentHeadPosition +
                " | Smjer: " + (movingUp ? " GORE" : " DOLE"));
        System.out.println("[SCAN DISK] Redoslijed opsluživanja: " + requests);

        int totalSeek = 0;
        for (int block : requests) {
            int distance = Math.abs(block - currentHeadPosition);
            totalSeek += distance;



            System.out.println("  -> Blok " + block + " (pomak: " + distance + ")");
            currentHeadPosition = block;
        }


        movingUp = !movingUp;
        System.out.println("[SCAN DISK] Ukupan pomak glave u ovom prolazu: " + totalSeek);

        String owner = (p == null) ? "KERNEL" : "PID:" + p.getPid();
        System.out.println("DiskDevice " + name + ": Operacija " + op.getType() + " uspješno završena za " + owner + ".\n");

        busy = false;
    }
}