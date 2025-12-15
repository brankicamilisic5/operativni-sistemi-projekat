package FS;

public abstract class FSNode {
    protected String name;
    protected Directory parent;

    public FSNode(String name, Directory parent){
        this.name=name;
        this.parent=parent;
    }

    public String getName() {
        return name;
    }

    public Directory getParent() {
        return parent;
    }
}
