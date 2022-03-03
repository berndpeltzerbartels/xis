package one.xis.utils.xml;

import lombok.RequiredArgsConstructor;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

@RequiredArgsConstructor
public class NodeIterator implements Iterator<Node> {
    private final NodeList nodeList;
    private int index;

    @Override
    public boolean hasNext() {
        return index < nodeList.getLength();
    }

    @Override
    public Node next() {
        if (index >= nodeList.getLength()) {
            throw new NoSuchElementException(this + ": next");
        }
        return nodeList.item(index++);
    }

    public Stream<Node> asStream() {
        Spliterator<Node> nodeSpliterator = Spliterators.spliterator(this, nodeList.getLength(), Spliterator.ORDERED);
        return StreamSupport.stream(nodeSpliterator, false);
    }
}
