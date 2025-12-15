package FS;

import java.util.ArrayList;
import java.util.List;

public class Directory extends FSNode {
    private List<FSNode> children;

    public Directory(String name, Directory parent) {
        super(name, parent);
        this.children = new ArrayList<>();
    }

    public void addChild(FSNode node) {
        children.add(node);
    }

    public FSNode getChild(String name) {
        for (FSNode n : children) {
            if (n.getName().equals(name))
                return n;
        }
        return null;
    }

    public List<FSNode> list() {
        return children;
    }
}
