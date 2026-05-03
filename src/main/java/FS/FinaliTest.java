package FS;

import DEVICE.DiskDevice;
import MEMORY.*;
import PROCES.*;
import OS.*;
import java.util.ArrayList;


class FinalniTest {

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
            File mojProgram = fs.createFile("/korisnik/projekat.asm");
            if (mojProgram != null) {
                mojProgram.write("LOAD 10\nADD 20\nHALT");
            }

            System.out.println("--- [SISTEM] SVE KOMPONENTE SU SPREMNE ---");

            Thread kernelThread = new Thread(() -> kernel.run());
            kernelThread.setDaemon(true);
            kernelThread.start();

            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            System.out.println("\n--- [INTERFEJS] SHELL JE AKTIVAN ---");

            Shell shell = new Shell(kernel, fs);
            shell.start();
        }

    }




