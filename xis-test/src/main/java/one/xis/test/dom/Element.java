package one.xis.test.dom;


import lombok.Getter;
import one.xis.utils.lang.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

@Getter
@SuppressWarnings("unused")
public class Element extends Node {

    public final String localName;
    public Node firstChild;
    public String innerHTML;
    public String innerText;
    public Object _handler;
    public final NodeList childNodes = new NodeList();
    private final Map<String, String> attributes = new HashMap<>();
    private final Collection<String> cssClasses = new HashSet<>();

    public Element(String tagName) {
        this.localName = tagName;
    }

    public void appendChild(Node node) {
        if (firstChild == null) {
            firstChild = node;
        } else {
            firstChild.getLastSibling().nextSibling = node;
        }
        node.parentNode = this;
        updateChildNodes();
    }

    public void insertBefore(Node newNoode, Node referenceNode) {
        if (referenceNode.parentNode != this) {
            throw new IllegalStateException();
        }
        referenceNode.insertPreviousSibling(newNoode);
        updateChildNodes();
    }

    public void removeChild(Node node) {
        if (node.parentNode != this) {
            throw new IllegalStateException("not a child");
        }
        node.remove();
    }

    public List<String> getAttributeNames() {
        return new ArrayList<>(attributes.keySet());
    }

    public String getAttribute(String name) {
        return attributes.get(name);
    }

    public void setAttribute(String name, String value) {
        attributes.put(name, value);
        if (name.equals("class")) {
            addClasses(value);
        }
    }

    public void removeAttribute(String name) {
        attributes.remove(name);
        if (name.equals("class")) {
            cssClasses.clear();
        }
    }

    public List<Node> getChildList() {
        return childNodes.list();
    }


    @Override
    public String getName() {
        return localName;
    }

    public List<Element> getChildElementsByName(String name) {
        return childNodes.getElementsByName(name);
    }

    public Element getChildElementByName(String name) {
        return childNodes.getElementByName(name);
    }

    private void addClasses(String classes) {
        cssClasses.addAll(Arrays.stream(classes.split(" "))
                .filter(StringUtils::isNotEmpty)
                .collect(Collectors.toSet()));

    }

    @Override
    public Node cloneNode() {
        var clone = new Element(localName);
        this.attributes.forEach(clone::setAttribute);
        childNodes.stream().forEach(child -> clone.appendChild(child.cloneNode()));
        return clone;
    }

    public Element childElement(int index) {
        var elementList = getChildElements();
        if (index >= elementList.size()) {
            return null;
        }
        return elementList.get(index);
    }

    public List<Element> getChildElements() {
        return childNodes.stream().filter(Element.class::isInstance).map(Element.class::cast).collect(Collectors.toList());
    }

    public List<String> getChildElementNames() {
        return childNodes.stream()
                .filter(Element.class::isInstance)
                .map(Element.class::cast)
                .map(Element::getLocalName)
                .collect(Collectors.toList());
    }

    public String getTextContent() {
        return childNodes.stream()
                .filter(TextNode.class::isInstance)
                .map(TextNode.class::cast)
                .map(TextNode::getNodeValue)
                .filter(Objects::nonNull)
                .collect(Collectors.joining()).trim();
    }

    void findByTagName(String name, NodeList result) {
        if (localName.equals(name)) {
            result.addNode(this);
        }
        if (nextSibling != null && nextSibling instanceof Element) {
            ((Element) nextSibling).findByTagName(name, result);
        }
        if (firstChild != null && firstChild instanceof Element) {
            ((Element) firstChild).findByTagName(name, result);
        }
    }

    void findByClass(String cssClass, List<Element> result) {
        if (cssClasses.contains(cssClass)) {
            result.add(this);
        }
        if (nextSibling != null && nextSibling instanceof Element) {
            ((Element) nextSibling).findByClass(cssClass, result);
        }
        if (firstChild != null && firstChild instanceof Element) {
            ((Element) firstChild).findByClass(cssClass, result);
        }
    }

    Element getElementById(String id) {
        if (id.equals(attributes.get(id))) {
            return this;
        }
        if (nextSibling != null && nextSibling instanceof Element) {
            ((Element) nextSibling).getElementById(id);
        }
        if (firstChild != null && firstChild instanceof Element) {
            ((Element) firstChild).getElementById(id);
        }
        return null;
    }

    @Override
    public String toString() {
        return "<" + localName + ">";
    }

    void updateChildNodes() {
        childNodes.clear();
        var child = firstChild;
        while (child != null) {
            childNodes.addNode(child);
            child = child.nextSibling;
        }
    }

}
