package FS;

public class File extends FSNode {
    private StringBuilder content;
    private int diskBlock;

    public File(String name, Directory parent) {
        super(name, parent);
        this.content = new StringBuilder();
        this.diskBlock = -1;
    }

    public int getDiskBlock() {
        return diskBlock;
    }

    public void setDiskBlock(int diskBlock) {
        this.diskBlock = diskBlock;
    }


    public String read(){
        return content.toString();
    }

    public void write(String data){
        content.setLength(0);
        content.append(data);
    }

    public void append(String data){
        content.append(data);
    }
}
