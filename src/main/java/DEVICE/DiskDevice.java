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

        List<Integer> requests = new ArrayList<>();

        try {
            String[] parts = op.getData().split(",");
            for(String part : parts) {
                requests.add(Integer.parseInt(part.trim()));
            }
        } catch (Exception e) {
            requests.add((int)(Math.random() * 128));
        }

        if (requests.isEmpty()) {
            System.out.println("[DISK] Primljen prazan zahtjev. Operacija se preskače.");
            new Thread(() -> {busy = false;}).start();
            return;
        }


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

    }
}