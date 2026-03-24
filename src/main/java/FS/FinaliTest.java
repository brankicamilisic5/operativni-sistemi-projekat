package FS;

import DEVICE.DiskDevice;
import MEMORY.*;
import PROCES.*;
import OS.*;


class FinalniTest {

        public static void main(String[] args) {

            RAM ram = new RAM(1024);
            MemoryManager mm = new MemoryManager(ram);


            BlockedQueue blockedQueue = new BlockedQueue();
            DiskDevice hdd = new DiskDevice("HDD", blockedQueue);


            FileSystem fs = new FileSystem(hdd);
            OSKernel kernel = new OSKernel(mm, fs);

            System.out.println("--- START SIMULACIJE ---");


            kernel.boot();
            fs.createDirectory("/korisnik");

            /* Kreiranje fajla i pisanje Asemblerskog koda
            LOAD 10 (stavi 10 u ACC), ADD 20 (ACC postane 30), HALT (kraj) */
            File mojProgram = fs.createFile("/korisnik/projekat.asm");
            mojProgram.write("LOAD 10\nADD 20\nHALT");

            System.out.println("\n--- POKRETANJE PROCESA (Scenario 1.7) ---");


            int pid = kernel.createProcess("/korisnik/projekat.asm", 1);

            if (pid != -1) {

                System.out.println("\nTabela procesa pre izvršavanja:");
                kernel.getProcessTable().forEach(p -> System.out.println("PID: " + p.getPid() + " Status: " + p.getState()));


                kernel.run();
            }

            System.out.println("\n--- STATUS MEMORIJE NAKON GAŠENJA ---");
            System.out.println("Preostali segmenti u RAM-u: " + mm.dumpMemory().substring(0, 50) + "...");
        }
    }
