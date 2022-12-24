package one.xis.context.mocks;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


public class NodeList {
    private final List<Node> items = new ArrayList<>();

    public int length;

    public NodeList() {

    }

    public NodeList(Collection<? extends Node> nodes) {
        items.addAll(nodes);
        length = items.size();
    }


    public Node item(int i) {
        return (Node) items.get(i);
    }

    public void addItem(Node node) {
        items.add(node);
        length = items.size();
    }

    public void remove(Node node) {
        items.remove(node);
        length = items.size();
    }

}
