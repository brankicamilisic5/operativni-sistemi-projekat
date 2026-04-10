package FS;

import DEVICE.DiskDevice;
import MEMORY.*;
import PROCES.*;
import OS.*;
import SYSCALL.*;

import java.util.ArrayList;
import java.util.Arrays;


class FinalniTest {

    public static void main(String[] args) {

        RAM ram = new RAM(1024);
        MemoryManager mm = new MemoryManager(ram);


        BlockedQueue blockedQueue = new BlockedQueue(new ArrayList<>());
        DiskDevice hdd = new DiskDevice("HDD", blockedQueue);


        FileSystem fs = new FileSystem(hdd);
        OSKernel kernel = new OSKernel(mm, fs);

        System.out.println("--- START SIMULACIJE ---");


        kernel.boot();
        fs.createDirectory("/korisnik");

        // Kreiranje fajla i pisanje Asemblerskog koda

        File mojProgram = fs.createFile("/korisnik/projekat.asm");
        mojProgram.write("LOAD 10\nADD 20\nHALT");

        System.out.println("\n--- POKRETANJE PROCESA ---");


        int pid = kernel.createProcess("/korisnik/projekat.asm", 1);

        if (pid != -1) {

            System.out.println("\nTabela procesa pre izvršavanja:");
            kernel.getProcessTable().forEach(p -> System.out.println("PID: " + p.getPid() + " Status: " + p.getState()));


            kernel.run();
        }

        System.out.println("\n--- STATUS MEMORIJE NAKON GAŠENJA ---");
        System.out.println("Preostali segmenti u RAM-u: " + mm.dumpMemory().substring(0, 50) + "...");

        System.out.println("\n--- START SCENARIO 2: STRES TEST MEMORIJE ---");
        System.out.println("Pokušaj alokacije prevelikog procesa:");
        kernel.createProcess("SystemMonitor", 1);


        for (int i = 0; i < 5; i++) {
            kernel.createProcess("/korisnik/projekat.asm", 1);
        }


        System.out.println("\n[SCENARIO 2] Nasilno gašenje procesa PID 3...");
        Syscall killRequest;
        killRequest = new Syscall(SyscallType.KILL, Arrays.asList("3"));
        kernel.handleSyscall(killRequest, null);

        kernel.run();

        kernel.boot();

        Shell shell = new Shell(kernel, fs);

        new Thread(() -> kernel.run()).start();

        shell.start();

    }
}


