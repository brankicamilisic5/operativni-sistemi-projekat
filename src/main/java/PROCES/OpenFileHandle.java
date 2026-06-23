package PROCES;

import FS.File;
import FS.FileMode;

public class OpenFileHandle {
    private File file;
    private int position;
    private FileMode mode;

    public OpenFileHandle(File file, int position, FileMode mode) {
        this.file = file;
        this.position = position;
        this.mode = mode;
    }

    public File getFile() { return file; }

    public String read() {
        if (mode != FileMode.READ) {
            System.out.println("[OpenFileHandle] Greška: fajl nije otvoren u READ modu!");
            return null;
        }
        return file.read();
    }

    public void write(String data) {
        if (mode != FileMode.WRITE) {
            System.out.println("[OpenFileHandle] Greška: fajl nije otvoren u WRITE modu!");
            return;
        }
        file.write(data);
    }
}