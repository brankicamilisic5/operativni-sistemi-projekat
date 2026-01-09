package FS;

import DEVICE.DiskDevice;
import PROCES.OpenFileHandle;

public class FileSystem {
    private Directory root;
    private DiskDevice disk;

    public FileSystem(DiskDevice disk) {
        this.disk = disk;
        this.root = new Directory("/", null);
    }

    public FSNode resolve(String path) {
        if (path == null || path.isEmpty() || path.equals("/")) return root;

        String[] parts = path.split("/");
        FSNode current = root;

        for (String part : parts) {
            if (part.isEmpty()) continue;
            if (current instanceof Directory) {
                current = ((Directory) current).getChild(part);
                if (current == null) return null;
            } else {
                return null;
            }
        }
        return current;
    }


    public Directory createDirectory(String path) {
        Directory newDir = new Directory(path, root);
        root.addChild(newDir);
        return newDir;
    }


    public File createFile(String name) {
        File newFile = new File(name, root);
        root.addChild(newFile);
        System.out.println("FileSystem: Kreiran fajl " + name);
        return newFile;
    }


    public void delete(String path) {
        FSNode node = resolve(path);
        if (node != null && node.getParent() != null) {
            System.out.println("Obrisano: " + path);
        }
    }

    public OpenFileHandle open(String path) {
        FSNode node = resolve(path);
        if (node instanceof File) {
            System.out.println("FileSystem: Otvoren handle za " + path);
            return new OpenFileHandle((File) node, 0, FileMode.WRITE);
        }
        System.out.println("FileSystem GREŠKA: Fajl nije pronađen na putanji " + path);
        return null;
    }


}
