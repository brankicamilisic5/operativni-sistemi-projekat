package IO;

public class IOOperation {
    private IOType type;
    private String data;
    private int duration;

    public IOOperation(IOType type, String data, int duration){
        this.type = type;
        this.data = data;
        this.duration = duration;
    }

    public String getData() {
        return data;
    }

    public int getDuration() {
        return duration;
    }

    public IOType getType() {
        return type;
    }

    @Override
    public String toString(){
        return "IOOperation{ " + " type= " + type + ", data= " + data + '\'' + ", duration= " + duration + '}';
    }
}
