package FS;

public class File {
    private StringBuilder content;

    public File(){
        this.content=new StringBuilder();
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
