package FS;

import DEVICE.DiskDevice;
import IO.IOOperation;
import IO.IOType;
import PROCES.OpenFileHandle;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class FileSystem {
    private Directory root;
    private DiskDevice disk;

    public FileSystem(DiskDevice disk) {
        this.disk = disk;
        this.root = new Directory("/", null);
    }

    public FSNode resolve(String path) {
        if (path == null || path.isEmpty() || path.equals("/")) {
            return root;
        }

        String[] parts = path.split("/");
        FSNode current = root;

        for (String part : parts) {
            if (part.isEmpty() || part.equals(".")) {
                continue;
            }
            if (part.equals("..")) {
                if (current.getParent() != null) {
                    current = current.getParent();
                }
                continue;
            }

            if (current instanceof Directory) {
                current = ((Directory) current).getChild(part);
                if (current == null) return null;
            } else {
                return null;
            }
        }
        return current;
    }

    private String getNameFromPath(String path) {
        if (path.endsWith("/") && path.length() > 1) {
            path = path.substring(0, path.length() - 1);
        }
        int lastSlash = path.lastIndexOf("/");
        return (lastSlash == -1) ? path : path.substring(lastSlash + 1);
    }

    private Directory getParentDirectory(String path) {
        int lastSlash = path.lastIndexOf("/");
        if (lastSlash <= 0) return root;

        String parentPath = path.substring(0, lastSlash);
        FSNode parent = resolve(parentPath);

        if (parent instanceof Directory) {
            return (Directory) parent;
        }
        return null;
    }

    private String getFullPath(FSNode node) {
        if (node.getParent() == null) return "/";
        return getFullPath(node.getParent()) + "/" + node.getName();
    }

    public Directory createDirectory(String path) {
        if (path == null || path.isEmpty() || path.equals("/")) return root;

        String[] parts = path.split("/");
        Directory current = root;

        for (String part : parts) {
            if (part.isEmpty() || part.equals(".") || part.equals("..")) continue;

            FSNode next = current.getChild(part);

            if (next == null) {
                Directory newDir = new Directory(part, current);
                current.addChild(newDir);
                System.out.println("Kreiran direktorijum: " + getFullPath(newDir));
                current = newDir;
            } else if (next instanceof Directory) {
                current = (Directory) next;
            } else {
                System.out.println("Greška: " + part + " je fajl!");
                return null;
            }
        }
        return current;
    }



    public File createFile(String path) {
        String name = getNameFromPath(path);
        Directory parent = getParentDirectory(path);

        FSNode existing = resolve(path);
        if (existing != null) {
            if (existing instanceof File) {
                return (File) existing;
            } else {
                System.out.println("Greška: Već postoji direktorijum sa imenom " + name);
                return null;
            }
        }
        if (parent == null) {
            System.out.println("Greška pri kreiranju fajla.");
            return null;
        }

        File file = new File(name, parent);
        parent.addChild(file);

        IOOperation op = new IOOperation(IOType.WRITE, "Metadata for " + name, 1);
        disk.startOperation(op, null);

        return file;
    }

    public void delete(String path) {
        FSNode node = resolve(path);
        if (node == null) {
            System.out.println("Greška: putanja ne postoji.");
            return;
        }
        if (node == root) {
            System.out.println("Greška: ne možete obrisati root direktorijum!");
            return;
        }

        cleanupResources(node);

        Directory parent = node.getParent();
        if (parent != null) {
            parent.removeChild(node.getName());
            System.out.println("Obrisano: " + path);
        }
    }

    private void cleanupResources(FSNode node) {
        if (node instanceof Directory dir) {
            for (FSNode child : dir.list()) {
                cleanupResources(child);
            }
        } else if (node instanceof File file) {
            IOOperation op = new IOOperation(IOType.WRITE, "DELETE " + file.getName(), 2);
            disk.startOperation(op, null);

            System.out.println("DiskDevice: Oslobođen prostor za " + file.getName());
        }
    }

    public OpenFileHandle open(String path) {
        FSNode node = resolve(path);
        if (node instanceof File) {
            IOOperation op = new IOOperation(IOType.READ, path, 1);
            disk.startOperation(op, null);

            System.out.println("FileSystem: Otvoren handle za " + path);
            return new OpenFileHandle((File) node, 0, FileMode.WRITE);
        }
        return null;
    }

    public String readFromTxt(String fileName) {
        try {
            String content = Files.readString(Paths.get(fileName));
            System.out.println("FileSystem: Uspješno pročitan fajl " + fileName);

            IOOperation op = new IOOperation(IOType.READ, "BOOT_LOAD", 5);
            disk.startOperation(op, null);

            return content;

        } catch (IOException e) {
            System.out.println("Greška: Nije moguće pročitati " + fileName + ". Provjerite da li fajl postoji.");
            return "DEFAULT_SYSTEM_DATA";
        }
    }

}
