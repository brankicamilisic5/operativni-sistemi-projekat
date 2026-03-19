package DEVICE;
import IO.IOOperation;


public class ConsoleDevice extends IODevice {

    public ConsoleDevice(String name) {
        super(name);
    }

    @Override
    public void startOperation(IOOperation op, PCB p) {
        busy = true;

        System.out.println("[CONSOLE] Operacija: " + op.getType());

        if (op.getType() == IOType.READ) {
            System.out.println("[CONSOLE] Čitanje: " + op.getData());
        } else {
            System.out.println("[CONSOLE] Ispis: " + op.getData());
        }

        try {
            Thread.sleep(op.getDuration());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        busy = false;

        System.out.println("[CONSOLE] Završeno za proces " + p.getPid());
    }
}