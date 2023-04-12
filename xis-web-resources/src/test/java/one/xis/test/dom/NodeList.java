package one.xis.test.dom;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class NodeList {

    private final List<Node> nodes;
    public int length;

    public NodeList() {
        this.nodes = new ArrayList<>();
        this.length = nodes.size();
    }

    public Node item(int index) {
        return nodes.get(index);
    }

    public Stream<Node> stream() {
        return nodes.stream();
    }

    void addNode(Node node) {
        nodes.add(node);
        length = nodes.size();
    }

    void clear() {
        nodes.clear();
        length = 0;
    }

}
