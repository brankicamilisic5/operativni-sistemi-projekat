package FS;

import DEVICE.DiskDevice;
import MEMORY.*;
import PROCES.*;
import OS.*;
import java.util.ArrayList;

public class FinaliTest {
    public static void main(String[] args) {

        RAM ram = new RAM(1024);
        MemoryManager mm = new MemoryManager(ram);
        BlockedQueue blockedQueue = new BlockedQueue(new ArrayList<>());
        DiskDevice hdd = new DiskDevice("HDD", blockedQueue);
        FileSystem fs = new FileSystem(hdd);
        OSKernel kernel = new OSKernel(mm, fs);

        System.out.println("--- [SISTEM] INICIJALIZACIJA U TOKU ---");
        kernel.boot();

        fs.createDirectory("/korisnik");
        System.out.println("--- [SISTEM] SVE KOMPONENTE SU SPREMNE ---");

        Shell shell = new Shell(kernel, fs);
        shell.start();
    }
}