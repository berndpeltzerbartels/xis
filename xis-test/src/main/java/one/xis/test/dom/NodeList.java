package one.xis.test.dom;

import one.xis.utils.lang.CollectorUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class NodeList {

    private final List<Node> nodes;
    public int length;

    public NodeList() {
        this.nodes = new ArrayList<>();
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

    List<Element> getElementsByName(String name) {
        return stream().filter(ElementImpl.class::isInstance)
                .map(ElementImpl.class::cast)
                .filter(e -> e.getLocalName().equals(name))
                .collect(Collectors.toList());
    }

    Element getElementByName(String name) {
        return stream().filter(ElementImpl.class::isInstance)
                .map(ElementImpl.class::cast)
                .filter(e -> e.getLocalName().equals(name))
                .collect(CollectorUtils.toOnlyOptional(list -> new IllegalStateException("too many results for " + name)))
                .orElse(null);
    }

    void addNode(Node node) {
        nodes.add(node);
        length = nodes.size();
    }

    void clear() {
        nodes.clear();
        length = 0;
    }

    void removeTextNodes() {
        var textNodes = nodes.stream().filter(TextNodeIml.class::isInstance).collect(Collectors.toSet());
        textNodes.forEach(nodes::remove);
    }

    Stream<Element> elements() {
        return stream().filter(ElementImpl.class::isInstance).map(ElementImpl.class::cast);
    }
}
