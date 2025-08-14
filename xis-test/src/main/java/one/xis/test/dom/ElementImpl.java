package one.xis.test.dom;


import lombok.Getter;
import lombok.NonNull;
import one.xis.test.js.Event;
import one.xis.utils.lang.StringUtils;
import org.graalvm.polyglot.Value;
import org.graalvm.polyglot.proxy.ProxyExecutable;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Getter
@SuppressWarnings("unused")
public class ElementImpl extends NodeImpl implements Element {

    public final String localName;
    public Node firstChild;
    public DOMStringList classList = new DOMStringList();
    public final int nodeType = 1;
    public final NodeList childNodes = new NodeList();
    private final Map<String, String> attributes = new HashMap<>();
    private final Map<String, Collection<Consumer<Object>>> eventListeners = new HashMap<>();
    public static ElementImpl elementInFocus;

    static final Set<String> MEMEBERS = Set.of("childNodes",
            "firstChild",
            "parentNode",
            "nextSibling",
            "localName",
            "tagName",
            "classList",
            "id",
            "innerText",
            "textContent",
            "innerHTML",
            // Methoden hinzufügen
            "getAttributeNames",
            "getAttribute",
            "setAttribute",
            "hasAttribute",
            "removeAttribute",
            "removeChild",
            "appendChild",
            "nodeType",
            "asString",
            "addEventListener",
            "click",
            "insertBefore",
            "cloneNode");

    public ElementImpl(@NonNull String tagName) {
        this.localName = tagName;
        setAttribute("childNodes", childNodes);
    }

    @Override
    public String getId() {
        return attributes.get("id");
    }

    public void appendChild(@NonNull Node node) {
        node.setNextSibling(null);
        if (firstChild == null) {
            setFirstChild(node);
        } else {
            var last = (NodeImpl) firstChild.getLastSibling();
            last.setNextSibling(node);
            if (last == last.getNextSibling()) {
                throw new IllegalStateException();
            }
        }

        ((NodeImpl) node).parentNode = this;
        updateChildNodes();

    }

    @Override
    public void insertBefore(Node node, Node referenceNode) {
        if (((NodeImpl) referenceNode).parentNode != this) {
            throw new IllegalStateException();
        }
        referenceNode.insertPreviousSibling(node);
        updateChildNodes();
    }

    public void removeChild(NodeImpl node) {
        if (node.parentNode != this) {
            return; //throw new IllegalStateException("not a child");
        }
        node.remove();
    }

    @Override
    public List<String> getAttributeNames() {
        return new ArrayList<>(attributes.keySet());
    }

    @Override
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

// in xis-test/src/main/java/one/xis/test/dom/ElementImpl.java

    private void setAttribute(String name, Value value) {
        if (value == null || value.isNull()) {
            setAttribute(name, (String) null);
            return;
        }

        String stringValue;
        if (value.isString()) {
            stringValue = value.asString();
        } else if (value.isNumber()) {
            // Konvertiert JS-Zahlen explizit in einen String.
            stringValue = value.as(Number.class).toString();
        } else if (value.isBoolean()) {
            // Konvertiert JS-Booleans explizit in einen String.
            stringValue = String.valueOf(value.asBoolean());
        } else {
            // Fallback, der versucht, eine allgemeine String-Konvertierung durchzuführen.
            stringValue = value.toString();
        }

        setAttribute(name, stringValue);
    }

    @Override
    public Collection<String> getCssClasses() {
        return classList.getValues();
    }

    @Override
    public boolean hasChildNodes() {
        return childNodes.length > 0;
    }

    public void setInnerText(String text) {
        this.childNodes.clear();
        this.setFirstChild(null);
        this.appendChild(new TextNodeIml(text));
    }

    @Override
    public List<Element> querySelectorAll(String selector, boolean firstOnly) {
        return Element.super.querySelectorAll(selector, firstOnly);
    }

    public void removeAttribute(String name) {
        attributes.remove(name);
        if (name.equals("class")) {
            classList.clear();
        }
    }

    public boolean hasAttribute(String name) {
        if ("childNodes".equals(name)) {
            return true; // childNodes is a special attribute
        }
        return attributes.containsKey(name);
    }

    @Override
    public List<Node> getChildList() {
        return childNodes.list();
    }

    @Override
    public String getName() {
        return localName;
    }

    @Override
    public List<Element> getChildElementsByName(String name) {
        return childNodes.getElementsByName(name);
    }

    @Override
    public Element getChildElementByName(String name) {
        return childNodes.getElementByName(name);
    }

    private void addClasses(String classes) {
        Arrays.stream(classes.split(" "))
                .filter(StringUtils::isNotEmpty)
                .forEach(classList::add);

    }

    @Override
    public NodeImpl cloneNode() {
        var clone = new ElementImpl(localName);
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


    public void removeChild(Node node) {
        if (node.getParentNode() != this) {
            return; //throw new IllegalStateException("not a child");
        }
        node.remove();
    }


    @Override
    public List<Element> findDescendants(Predicate<Element> predicate) {
        var result = new ArrayList<Element>();
        for (var child : this.getChildElements()) {
            child.findElements(predicate, result);
        }
        return result;
    }

    @Override
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

    @Override
    public List<Element> getChildElements() {
        return childNodes.stream().filter(ElementImpl.class::isInstance).map(ElementImpl.class::cast).collect(Collectors.toList());
    }

    @Override
    public List<String> getChildElementNames() {
        return childNodes.stream()
                .filter(ElementImpl.class::isInstance)
                .map(ElementImpl.class::cast)
                .map(Element::getLocalName)
                .collect(Collectors.toList());
    }

    @Override
    public String getTextContent() {
        return childNodes.stream()
                .filter(TextNodeIml.class::isInstance)
                .map(TextNodeIml.class::cast)
                .map(TextNode::getNodeValue)
                .map(StringUtils::toString)
                .filter(Objects::nonNull)
                .collect(Collectors.joining()).trim();
    }

    @Override
    public Element getDescendantById(String id) {
        if (id.equals(attributes.get("id"))) {
            return this;
        }
        if (nextSibling != null && nextSibling instanceof ElementImpl) {
            var element = ((Element) nextSibling).getDescendantById(id);
            if (element != null) {
                return element;
            }
        }
        if (firstChild != null && firstChild instanceof ElementImpl) {
            var element = ((Element) firstChild).getDescendantById(id);
            if (element != null) {
                return element;
            }
        }
        return null;
    }

    @Override
    public void click() {
        focus(this);
        fireEvent("click");
    }

    @Override
    public String getInnerText() {
        var sb = new StringBuilder();
        for (var child : childNodes.list()) {
            if (child instanceof TextNodeIml) {
                sb.append(((TextNode) child).getNodeValue());
            } else if (child instanceof ElementImpl) {
                sb.append(((Element) child).getInnerText());
            }
        }
        return sb.toString();
    }

    protected void focus(ElementImpl element) {
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


    @Override
    public void findByTagName(String name, NodeList result) {
        if (localName.equals(name)) {
            result.addNode(this);
        }
        if (nextSibling != null && nextSibling instanceof ElementImpl) {
            ((Element) nextSibling).findByTagName(name, result);
        }
        if (firstChild != null && firstChild instanceof ElementImpl) {
            ((Element) firstChild).findByTagName(name, result);
        }
    }

    @Override
    public void findByClass(String cssClass, List<Element> result) {
        if (classList.contains(cssClass)) {
            result.add(this);
        }
        if (nextSibling != null && nextSibling instanceof ElementImpl) {
            ((Element) nextSibling).findByClass(cssClass, result);
        }
        if (firstChild != null && firstChild instanceof ElementImpl) {
            ((Element) firstChild).findByClass(cssClass, result);
        }
    }

    @Override
    public void findElements(Predicate<Element> predicate, Collection<Element> result) {
        if (predicate.test(this)) {
            result.add(this);
        }
        var sibling = nextSibling;
        while (sibling != null) {
            if (sibling instanceof Element element) {
                element.findElements(predicate, result);
                break;
            }
            sibling = sibling.getNextSibling();
        }
        if (firstChild != null && firstChild instanceof Element element) {
            element.findElements(predicate, result);
        }
    }


    @Override
    public TextNode getTextNode() {
        var child = firstChild;
        while (child != null) {
            if (child instanceof TextNodeIml) {
                return (TextNode) child;
            }
            if (child instanceof Element element) {
                child = element.getNextSibling();
            }
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

    @Override
    public List<Element> getChildElementsByClassName(String cssClass) {
        return getChildElements().stream().filter(e -> e.getClassList().contains(cssClass)).collect(Collectors.toList());
    }

    @Override
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

    void updateChildNodes() {
        childNodes.clear();
        var child = firstChild;
        while (child != null) {
            childNodes.addNode(child);
            child = child.getNextSibling();
        }
    }

    public void setFirstChild(Node node) {
        if (this.equals(node)) {
            throw new IllegalStateException();
        }
        firstChild = node;
    }

    @Override
    public String asString() {
        var builder = new StringBuilder();
        evaluateContent(builder, 0);
        return builder.toString();
    }

    /**
     * Finds the first descendant element that matches the specified CSS selector.
     *
     * @param selector The CSS selector string.
     * @return The first matching Element, or null if no match is found.
     */
    @Override
    public Element querySelector(String selector) {
        List<Element> results = querySelectorAll(selector, true);
        return results.isEmpty() ? null : results.get(0);
    }

    /**
     * Finds all descendant elements that match the specified CSS selector.
     *
     * @param selector The CSS selector string.
     * @return A List of matching Elements. The list is empty if no matches are found.
     */
    @Override
    public List<Element> querySelectorAll(String selector) {
        return querySelectorAll(selector, false);
    }

    String asString(int indent) {
        var builder = new StringBuilder();
        evaluateContent(builder, indent);
        return builder.toString();
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
        childNodes.stream().forEach(node -> ((NodeImpl) node).evaluateContent(builder, indent + 1));
        for (int i = 0; i < indent; i++) {
            builder.append("\t");
        }
        builder.append("</");
        builder.append(localName);
        builder.append(">\n");
    }


    private String innerHtmlAsString() {
        var s = new StringBuilder();
        var e = firstChild;
        while (e != null) {
            s.append(e.asString());
            e = e.getNextSibling();
        }
        return s.toString();
    }

    @Override
    public Object getMember(String key) {
        return switch (key) {
            // Felder
            case "childNodes" -> this.childNodes;
            case "firstChild" -> this.firstChild;
            case "parentNode" -> this.parentNode;
            case "nextSibling" -> this.nextSibling;
            case "localName", "tagName" -> this.localName;
            case "classList" -> this.classList;
            case "id" -> getId();
            case "innerText" -> getInnerText();
            case "textContent" -> getTextContent();
            case "innerHTML" -> innerHtmlAsString();

            // Methoden als aufrufbare ProxyExecutables
            case "getAttribute" -> (ProxyExecutable) arguments -> getAttribute(arguments[0].asString());
            case "hasAttribute" -> (ProxyExecutable) arguments -> hasAttribute(arguments[0].asString());
            case "removeChild" -> (ProxyExecutable) arguments -> {
                if (arguments[0].isProxyObject()) {
                    removeChild((Node) arguments[0].asProxyObject());
                }
                return null; // JS-Funktionen geben oft undefined (null) zurück
            };
            case "appendChild" -> (ProxyExecutable) arguments -> {
                if (arguments[0].isProxyObject()) {
                    appendChild((Node) arguments[0].asProxyObject());
                }
                return null;
            };
            case "addEventListener" -> (ProxyExecutable) arguments -> {
                Value type = arguments[0];
                Value listener = arguments[1];
                addEventListener(type.asString(), event -> listener.execute(event));
                return null;
            };
            case "getAttributeNames" -> (ProxyExecutable) arguments -> getAttributeNames().toArray(new String[0]);
            case "removeAttribute" -> (ProxyExecutable) arguments -> {
                removeAttribute(arguments[0].asString());
                return null;
            };
            case "nodeType" -> nodeType;
            case "asString" -> (ProxyExecutable) arguments -> asString();
            case "setAttribute" -> (ProxyExecutable) arguments -> { // Hinzugefügt
                setAttribute(arguments[0].asString(), arguments[1]);
                return null; // setAttribute gibt in JS undefined zurück
            };
            case "click" -> (ProxyExecutable) arguments -> {
                click();
                return null; // click gibt in JS undefined zurück
            };
            case "insertBefore" -> (ProxyExecutable) arguments -> {
                if (arguments[0].isProxyObject() && arguments[1].isProxyObject()) {
                    insertBefore(arguments[0].asProxyObject(), arguments[1].asProxyObject());
                }
                return null; // insertBefore gibt in JS undefined zurück
            };

            case "cloneNode" -> (ProxyExecutable) arguments -> cloneNode();
            // Fallback auf die Attribut-Map
            default -> attributes.get(key);
        };
    }

    @Override
    public boolean hasMember(String key) {
        return MEMEBERS.contains(key);
    }

    @Override
    public Object getMemberKeys() {
        return new ArrayList<>(MEMEBERS);
    }

    @Override
    public void putMember(String key, Value value) {
        attributes.put(key, value == null ? "" : value.asString());
        switch (key) {
            case "class" -> {
                classList.clear();
                if (value != null && value.isString()) {
                    addClasses(value.asString());
                }
            }
            case "id", "title", "style" -> attributes.put(key, value == null ? "" : value.asString());
            case "innerText" -> {
                if (!value.isString()) {
                    throw new IllegalArgumentException("innerText must be a String, but was: " + value.getClass());
                }
                setInnerText(value.asString());
            }
            case "innerHTML" -> {
                if (value == null || value.isNull()) {
                    firstChild = null;
                } else if (value.isString()) {
                    firstChild = Element.of(value.asString());
                } else {
                    throw new IllegalArgumentException("innerHTML must be a String or null, but was: " + value.getClass());
                }
                updateChildNodes();
            }
        }
    }

    @Override
    public boolean removeMember(String key) {
        return super.removeMember(key);
    }

}