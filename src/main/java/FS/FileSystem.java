package FS;

import DEVICE.DiskDevice;
import PROCES.OpenFileHandle;

public class FileSystem {
    private Directory root;
    private DiskDevice disk;

    public FileSystem(DiskDevice disk) {
        this.disk = disk;
        this.root = new Directory("", null);
    }

    public Directory makeDirectory(String path) {
        return null;
    }

    public File createFile(String path) {
        return null;
    }
    public void delte(String path){}

    public FSNode resolve(String path){
        return null;
    }

    public OpenFileHandle open(String path){
        return null;
    }


}
