package MEMORY;

import OS.OSKernel;
import PROCES.PCB;
import PROCES.ProcessState;

public class DMAController {

    private OSKernel kernel;
    private boolean busy = false;

    private int transferCount = 0;
    private long totalBytesTransferred = 0;

    public DMAController(OSKernel kernel) {
        this.kernel = kernel;
    }

    public void transfer(String source, String dest, String data, PCB pcb) {

        if (busy) {
            System.out.println("[DMA] zauzet");
            return;
        }

        busy = true;
        int dataSize =(data == null) ? 0 : data.length();

        System.out.println("[DMA] start " + source + " -> " + dest);

        if (pcb!=null) {
            pcb.setState(ProcessState.WAITING);
            kernel.getBlockedQueue().block(pcb);
        }

        new Thread(() -> {

            try {
                Thread.sleep(Math.max(100, dataSize * 2));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            transferCount++;
            totalBytesTransferred += dataSize;
            busy = false;

            System.out.println("[DMA] done " + source + " -> " + dest);

            if (pcb != null) {
                kernel.getBlockedQueue().unblock(pcb);
                pcb.setState(ProcessState.READY);
                kernel.getReadyQueue().add(pcb);
            }

        }).start();
    }

    public boolean isBusy() {
        return busy;
    }

    public int getTransferCount() {
        return transferCount;
    }

    public long getTotalBytesTransferred() {
        return totalBytesTransferred;
    }
}