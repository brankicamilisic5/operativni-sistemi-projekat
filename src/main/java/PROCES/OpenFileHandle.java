package PROCES;

import FS.File;
import FS.FileMode;

public class OpenFileHandle {
    private File file;
    private int position;
    private FileMode mode;

    public OpenFileHandle(File file, int position, FileMode mode){
        this.file=file;
        this.position=position;
        this.mode=mode;
    }

}
