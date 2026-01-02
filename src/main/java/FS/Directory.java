package FS;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Directory extends FSNode {
    private Map<String,FSNode> children;

    public Directory(String name, Directory parent) {
        super(name, parent);
        this.children = new HashMap<>();
    }

    public void addChild(FSNode node) {
        children.put(node.getName(), node);
    }

    public FSNode getChild(String name) {
        return children.get(name);
    }

    public List<FSNode> list() {
        return new ArrayList<>(children.values());
    }
}
