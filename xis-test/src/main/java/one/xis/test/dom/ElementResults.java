package one.xis.test.dom;


import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static one.xis.test.dom.DomAssert.assertTrue;

@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class ElementResults {

    private final List<Element> elements;

    public ElementResults assertSize(int size) {
        assertTrue(elements.size() == size, "Expected result to be of size %d, but it was %d", size, elements.size());
        return this;
    }

    public ElementResults assertEmpty() {
        assertTrue(elements.isEmpty(), "Expected result to be empty, but it contains %d elements", elements.size());
        return this;
    }

    public int size() {
        return elements.size();
    }

    public ElementResult toUniqueResult() {
        switch (elements.size()) {
            case 0:
                throw new DomAssertionException("Can not convert to unique result, because result is empty");
            case 1:
                return new ElementResult(elements.get(0));
            default:
                throw new DomAssertionException("Can not convert to unique result, because there ars %d elements", elements.size());
        }

    }

    public ElementResult pick(String name) {
        var result = pickAll(name);
        switch (result.size()) {
            case 0:
                throw new DomAssertionException("Can not pick element <%s> to unique result, because result is empty", name);
            case 1:
                return result.pick(0);
            default:
                throw new DomAssertionException("Can not pick element <%s> to unique result, because there are too many of them (%d)", name, result.size());
        }

    }

    public ElementResult pick(int index) {
        return new ElementResult(elements.get(index));
    }

    public ElementResults pickAll(String name) {
        return new ElementResults(elements.stream().filter(e -> e.getLocalName().equals(name)).collect(Collectors.toList()));
    }

    public void andThen(Consumer<List<Element>> elementConsumer) {
        elementConsumer.accept(Collections.unmodifiableList(elements));
    }
}
