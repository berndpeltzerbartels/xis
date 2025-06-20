package one.xis.test.dom;


import lombok.Getter;
import lombok.NonNull;
import one.xis.test.js.Event;
import one.xis.utils.lang.StringUtils;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Getter
@SuppressWarnings("unused")
public class Element extends Node {

    // TODO interface, not everyrhing here should be accesssible
    public final String localName;
    public Node firstChild;
    public String innerText = "";
    public DOMStringList classList = new DOMStringList();
    public Object handler;
    public Object _rootHandler;
    public Collection<Object> _attributes;
    public String _widgetId;
    public Map<String, Object> _removedAttributes;

    public final int nodeType = 1;
    public final NodeList childNodes = new NodeList();
    private final Map<String, String> attributes = new HashMap<>();
    private final Map<String, Collection<Consumer<Object>>> eventListeners = new HashMap<>();

    public static Element elementInFocus;

    public Element(@NonNull String tagName) {
        this.localName = tagName;
    }

    public String getId() {
        return attributes.get("id");
    }


    public void appendChild(@NonNull Node node) {
        node.setNextSibling(null);
        if (firstChild == null) {
            setFirstChild(node);
        } else {
            var last = firstChild.getLastSibling();
            last.setNextSibling(node);
            if (last == last.nextSibling) {
                throw new IllegalStateException();
            }
        }

        node.parentNode = this;
        updateChildNodes();
        if (node instanceof TextNode) {
            textNodeChanged();
        }
    }

    public void insertBefore(Node node, Node referenceNode) {
        if (referenceNode.parentNode != this) {
            throw new IllegalStateException();
        }
        referenceNode.insertPreviousSibling(node);
        updateChildNodes();
        if (node instanceof TextNode) {
            textNodeChanged();
        }
    }

    public void removeChild(Node node) {
        if (node.parentNode != this) {
            return; //throw new IllegalStateException("not a child");
        }
        node.remove();
        if (node instanceof TextNode) {
            textNodeChanged();
        }
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


    public void setAttribute(String name, Object value) {
        setAttribute(name, value == null ? null : value.toString());
    }

    public Collection<String> getCssClasses() {
        return classList.getValues();
    }

    public boolean hasChildNodes() {
        return childNodes.length > 0;
    }

    public void setInnerText(String text) {
        innerText = text;
        this.childNodes.clear();
        this.setFirstChild(null);
        this.appendChild(new TextNode(text));
        textNodeChanged();
    }

    public void removeAttribute(String name) {
        attributes.remove(name);
        if (name.equals("class")) {
            classList.clear();
        }
    }

    public boolean hasAttribute(String name) {
        return attributes.containsKey(name);
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
        Arrays.stream(classes.split(" "))
                .filter(StringUtils::isNotEmpty)
                .forEach(classList::add);

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

    public List<Element> findDescendants(Predicate<Element> predicate) {
        var result = new ArrayList<Element>();
        for (var child : this.getChildElements()) {
            child.findElements(predicate, result);
        }
        return result;
    }

    public Element findDescendant(Predicate<Element> predicate) {
        var result = new ArrayList<>(findDescendants(predicate));
        return switch (result.size()) {
            case 0 -> throw new NoSuchElementException();
            case 1 -> result.get(0);
            default -> throw new IllegalStateException("too many results");
        };
    }

    public void addEventListener(String name, Consumer<Object> listener) {
        eventListeners.computeIfAbsent(name, n -> new HashSet<>()).add(listener);
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
        var text = childNodes.stream()
                .filter(TextNode.class::isInstance)
                .map(TextNode.class::cast)
                .map(TextNode::getNodeValue)
                .map(StringUtils::toString)
                .filter(Objects::nonNull)
                .collect(Collectors.joining()).trim();
        if (text.isEmpty() && innerText != null) { // TODO create interface for Element and remove this hack
            text = text.concat(innerText);
        }
        return text;
    }

    public Element getDescendantById(String id) {
        if (id.equals(attributes.get("id"))) {
            return this;
        }
        if (nextSibling != null && nextSibling instanceof Element) {
            var element = ((Element) nextSibling).getDescendantById(id);
            if (element != null) {
                return element;
            }
        }
        if (firstChild != null && firstChild instanceof Element) {
            var element = ((Element) firstChild).getDescendantById(id);
            if (element != null) {
                return element;
            }
        }
        return null;
    }

    public void click() {
        focus(this);
        fireEvent("click");
    }

    public String getInnerText() {
        var sb = new StringBuilder();
        for (var child : childNodes.list()) {
            if (child instanceof TextNode) {
                sb.append(((TextNode) child).getNodeValue());
            } else if (child instanceof Element) {
                sb.append(((Element) child).getInnerText());
            }
        }
        return sb.toString();
    }

    protected void focus(Element element) {
        if (elementInFocus != this) {
            if (elementInFocus != null) {
                elementInFocus.fireEvent("blur");
                element.fireEvent("focus");
            }
            elementInFocus = this;
        }
    }

    protected void fireEvent(String eventType) {
        var listeners = eventListeners.get(eventType);
        if (listeners != null) {
            var event = new Event(eventType);
            listeners.forEach(listener -> listener.accept(event));
        }
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
        if (classList.contains(cssClass)) {
            result.add(this);
        }
        if (nextSibling != null && nextSibling instanceof Element) {
            ((Element) nextSibling).findByClass(cssClass, result);
        }
        if (firstChild != null && firstChild instanceof Element) {
            ((Element) firstChild).findByClass(cssClass, result);
        }
    }

    void findElements(Predicate<Element> predicate, Collection<Element> result) {
        if (predicate.test(this)) {
            result.add(this);
        }
        var sibling = nextSibling;
        while (sibling != null) {
            if (sibling instanceof Element element) {
                element.findElements(predicate, result);
                break;
            }
            sibling = sibling.nextSibling;
        }
        if (firstChild != null && firstChild instanceof Element element) {
            element.findElements(predicate, result);
        }
    }


    public TextNode getTextNode() {
        var child = firstChild;
        while (child != null) {
            if (child instanceof TextNode) {
                return (TextNode) child;
            }
            child = child.nextSibling;
        }
        return null;
    }

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder();
        s.append(super.toString());
        s.append(" ");
        s.append("<");
        s.append(localName);
        if (!attributes.isEmpty()) {
            s.append(" ");
            s.append(attributes.entrySet().stream().map(e -> String.format("%s=\"%s\"", e.getKey(), e.getValue())).collect(Collectors.joining(" ")));
        }
        s.append(">");
        return s.toString();
    }

    public List<Element> getChildElementsByClassName(String cssClass) {
        return getChildElements().stream().filter(e -> e.getClassList().contains(cssClass)).collect(Collectors.toList());
    }

    public List<Element> getDescendantElementsByClassName(String cssClass) {
        var result = new ArrayList<Element>();
        for (var child : getChildElements()) {
            if (child.getClassList().contains(cssClass)) {
                result.add(child);
            }
            result.addAll(child.getDescendantElementsByClassName(cssClass));
        }
        return result;
    }

    public void innerTextChanged() {
        this.childNodes.clear();
        this.firstChild = null;
        this.appendChild(new TextNode(innerText));
    }

    void textNodeChanged() {
        innerText = innerText();
    }

    void updateChildNodes() {
        childNodes.clear();
        var child = firstChild;
        while (child != null) {
            childNodes.addNode(child);
            child = child.nextSibling;
        }
    }

    public void setFirstChild(Node node) {
        if (this.equals(node)) {
            throw new IllegalStateException();
        }
        firstChild = node;
    }

    public String asString() {
        var builder = new StringBuilder();
        evaluateContent(builder, 0);
        return builder.toString();
    }

    String asString(int indent) {
        var builder = new StringBuilder();
        evaluateContent(builder, indent);
        return builder.toString();
    }

    private String innerText() {
        return childNodes.stream()
                .filter(TextNode.class::isInstance)
                .map(TextNode.class::cast)
                .map(TextNode::getNodeValue)
                .filter(Objects::nonNull)
                .map(Object::toString)
                .collect(Collectors.joining());
    }

    @Override
    protected void evaluateContent(StringBuilder builder, int indent) {
        for (int i = 0; i < indent; i++) {
            builder.append("\t");
        }
        builder.append("<");
        builder.append(localName);
        attributes.forEach((key, value) -> {
            builder.append(" ");
            builder.append(key);
            builder.append("=\"");
            builder.append(value);
            builder.append("\"");
        });
        if (childNodes.list().isEmpty()) {
            builder.append("/>\n");
            return;
        }
        builder.append(">\n");
        childNodes.stream().forEach(node -> node.evaluateContent(builder, indent + 1));
        for (int i = 0; i < indent; i++) {
            builder.append("\t");
        }
        builder.append("</");
        builder.append(localName);
        builder.append(">\n");
    }

}