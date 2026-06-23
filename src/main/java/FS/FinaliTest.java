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
        initSistem(kernel, fs);
        System.out.println("--- [SISTEM] SVE KOMPONENTE SU SPREMNE ---");

        Shell shell = new Shell(kernel, fs);
        shell.start();
    }

    private static void initSistem(OSKernel kernel, FileSystem fs) {

        fs.createDirectory("/Sistem");
        fs.createDirectory("/User");
        fs.createDirectory("/Data");
        fs.createDirectory("/Aplikacije");
        fs.createDirectory("/korisnik");

        FS.File f1 = fs.createFile("/Data/processor1.asm");
        if (f1 != null) f1.write("LOAD 12\nADD 8\nMUL 5\nPRINT\nHALT");

        FS.File f2 = fs.createFile("/Data/processor2.asm");
        if (f2 != null) f2.write("LOAD 25\nMUL 2\nADD 50\nPRINT\nHALT");

        FS.File f3 = fs.createFile("/Data/processor3.asm");
        if (f3 != null) f3.write("LOAD 100\nADD 50\nDIV 10\nPRINT\nHALT");

        FS.File f4 = fs.createFile("/User/calc1.asm");
        if (f4 != null) f4.write("LOAD 10\nADD 20\nMUL 3\nPRINT\nHALT");

        FS.File f5 = fs.createFile("/User/beskonacni1.asm");
        if (f5 != null) f5.write("LOAD 7\nADD 3\nMUL 2\nPRINT\nJMP 0");

        FS.File f6 = fs.createFile("/User/beskonacni2.asm");
        if (f6 != null) f6.write("LOAD 15\nSUB 5\nMUL 3\nPRINT\nJMP 0");

        FS.File f7 = fs.createFile("/Aplikacije/app1.asm");
        if (f7 != null) f7.write("LOAD 5\nADD 5\nMUL 2\nPRINT\nHALT");

        FS.File f8 = fs.createFile("/User/syscall_test.asm");
        if (f8 != null) f8.write("LOAD 5\nADD 3\nSYSCALL\nPRINT\nHALT");

        kernel.createProcess("/Data/processor1.asm", 1);
        kernel.createProcess("/Data/processor2.asm", 1);
        kernel.createProcess("/Data/processor3.asm", 1);
    }
}