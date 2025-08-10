package one.xis.test.dom;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

public class NodeList {

    private final List<Node> nodes;
    public int length;

    public NodeList() {
        this.nodes = new ArrayList<>();
    }

    public NodeList(List<Node> nodes) {
        this.nodes = new ArrayList<>(nodes);
        this.length = nodes.size();
    }

    public Node item(int index) {
        return nodes.get(index);
    }

    public Stream<Node> stream() {
        return list().stream();
    }

    public List<Node> list() {
        return Collections.unmodifiableList(nodes);
    }

    public boolean isEmpty() {
        return nodes.isEmpty();
    }

    void addNode(Node node) {
        nodes.add(node);
        length = nodes.size();
    }

    void clear() {
        nodes.clear();
        length = 0;
    }

    void updateChildNodes(NodeImpl element) {
        clear();
        var child = element.getFirstChild();
        while (child != null) {
            addNode(child);
            child = child.getNextSibling();
        }
    }
}
