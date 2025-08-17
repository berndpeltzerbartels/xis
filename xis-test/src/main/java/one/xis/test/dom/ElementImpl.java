package one.xis.test.dom;

import lombok.Getter;
import lombok.NonNull;
import one.xis.test.js.Event;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;


@Getter
@SuppressWarnings("unused")
public class ElementImpl extends NodeImpl implements Element {

    public final String localName;
    public final DOMStringList classList = new DOMStringList();
    public final int nodeType = 1;
    private final Map<String, String> attributes = new HashMap<>();
    private final Map<String, Collection<Consumer<Object>>> eventListeners = new HashMap<>();

    public ElementImpl(String localName) {
        super(NodeImpl.ELEMENT_NODE);
        this.localName = localName;
    }

    @Override
    public void setAttribute(String name, String value) {
        if (name.equals("class")) {
            classList.clear();
            for (String item : value.split(" ")) {
                if (!item.isBlank()) {
                    classList.add(item);
                }
            }
        }
        attributes.put(name, value);
    }

    @Override
    public void removeChild(Node b) {
        var node = getChildNodes().stream().filter(n -> n == b).findFirst().orElseThrow(() -> new IllegalStateException("Node not found"));
        node.remove();
    }

    @Override
    public void insertBefore(@NonNull Node before, @NonNull Node marker) {
        var previousChild = ((NodeImpl) marker).getPreviousSibling();
        if (previousChild == null) {
            setFirstChild((NodeImpl) before);
        } else {
            previousChild.setNextSibling((NodeImpl) before);
        }
        ((NodeImpl) before).setNextSibling((NodeImpl) marker);
        ((NodeImpl) before).setParentNode(this);
        updateChildNodes();
    }

    @Override
    public List<String> getAttributeNames() {
        return new ArrayList<>(attributes.keySet());
    }

    @Override
    public String getAttribute(String name) {
        if (name.equals("class")) {
            return String.join(" ", classList.getValues());
        }
        // We do not to have take care for getters here, because those are handled as attributes, too
        return attributes.get(name);
    }

    @Override
    public Element querySelector(String selector) {
        List<Element> results = querySelectorAll(selector, true);
        return results.isEmpty() ? null : results.get(0);
    }

    @Override
    public List<Element> querySelectorAll(String selector) {
        return querySelectorAll(selector, false);
    }

    @Override
    public ElementImpl cloneNode() {
        var clone = new ElementImpl(localName);
        this.getAttributes().forEach(clone::setAttribute);
        this.classList.getValues().forEach(clone.classList::add);
        getChildNodes().stream().forEach(child -> clone.appendChild(child.cloneNode()));
        return clone;
    }


    @Override
    public Element getElementById(@NonNull String id) {
        if (id.equals(getAttribute("id"))) {
            return this;
        }
        var child = getFirstChild();
        while (child != null) {
            if (child instanceof ElementImpl element) {
                var result = element.getElementById(id);
                if (result != null) {
                    return result;
                }
            }
            child = child.getNextSibling();
        }
        return null;
    }

    @Override
    public NodeList getElementsByTagName(String name) {
        var list = new ArrayList<Node>();
        findElements(e -> e.getLocalName().equals(name), list);
        return new NodeList(list);

    }

    @Override
    public void click() {
        fireEvent("click", new Event("click"));
    }

    @Override
    public List<Element> getChildElements() {
        return getChildNodes().stream()
                .filter(Element.class::isInstance)
                .map(Element.class::cast)
                .toList();
    }

    @Override
    public Collection<String> getCssClasses() {
        return classList.getValues();
    }


    @Override
    public boolean hasAttribute(String name) {
        if (name.equals("class")) {
            return classList.length > 0;
        }
        return attributes.containsKey(name);
    }

    public void removeAttribute(String name) {
        if (name.equals("class")) {
            classList.clear();
        }
        attributes.remove(name);
    }


    void addEventListener(String event, Consumer<Object> listener) {
        eventListeners.computeIfAbsent(event, k -> new ArrayList<>()).add(listener);
    }

    void fireEvent(String event, Object data) {
        if (eventListeners.containsKey(event)) {
            for (var listener : eventListeners.get(event)) {
                listener.accept(data);
            }
        }
    }

    Element getElementByTagName(String name) {
        var child = getFirstChild();
        while (child != null) {
            if (child instanceof ElementImpl element && name.equals(element.getLocalName())) {
                return element;
            }
            child = child.getNextSibling();
        }
        return null;
    }

    @Override
    public String getInnerHTML() {
        var sb = new StringBuilder();
        for (var child : getChildNodes().list()) {
            if (child instanceof ElementImpl element) {
                sb.append(element.asString());
            } else if (child instanceof TextNode textNode) {
                sb.append(textNode.getNodeValue());
            }
        }
        return sb.toString();
    }

    void setInnerHTML(String html) {
        getChildNodes().clear();
        var doc = Jsoup.parseBodyFragment(html);
        var body = doc.body();
        for (var child : body.childNodes()) {
            if (child instanceof org.jsoup.nodes.Element element) {
                var newElement = Element.createElement(element.tagName());
                newElement.setInnerText(element.ownText());
                for (var attr : element.attributes()) {
                    newElement.setAttribute(attr.getKey(), attr.getValue());
                }
                appendChild(newElement);
            } else if (child instanceof org.jsoup.nodes.TextNode textNode) {
                appendChild(new TextNodeIml(textNode.text()));
            }
        }
        updateChildNodes();
    }

    @Override
    public String getInnerText() {
        var sb = new StringBuilder();
        for (var child : getChildNodes().list()) {
            if (child instanceof TextNode textNode) {
                sb.append(textNode.getNodeValue());
            } else if (child instanceof ElementImpl element) {
                sb.append(element.getInnerText());
            }
        }
        return sb.toString();
    }

    @Override
    public Node findDescendant(Predicate<Node> predicate) {
        var descendants = findDescendants(predicate);
        return descendants.isEmpty() ? null : descendants.get(0);
    }

    @Override
    public List<Node> findDescendants(Predicate<Node> predicate) {
        var result = new ArrayList<Node>();
        findDescendants(predicate, result);
        return result;
    }

    @Override
    public String getTagName() {
        return localName;
    }

    @Override
    public List<Element> getElementsByClass(String item) {
        var list = new ArrayList<Node>();
        if (classList.contains(item)) {
            list.add(this);
        }
        findElements(e -> e.classList.contains(item), list);
        return list.stream().filter(Element.class::isInstance)
                .map(Element.class::cast)
                .toList();
    }

    void findDescendants(Predicate<Node> predicate, Collection<Node> result) {
        var child = getFirstChild();
        while (child != null) {
            if (child instanceof ElementImpl element) {
                if (predicate.test(element)) {
                    result.add(element);
                }
                element.findDescendants(predicate, result);
            }
            child = child.getNextSibling();
        }
    }

    void setTextContent(String text) {
        getChildNodes().clear();
        setFirstChild(new TextNodeIml(text));
        updateChildNodes();
    }

    ElementImpl findChildByName(String name) {
        var child = getFirstChild();
        while (child != null) {
            if (child instanceof ElementImpl element && name.equals(element.getLocalName())) {
                return element;
            }
            child = child.getNextSibling();
        }
        return null;
    }

    ElementImpl findChildById(String id) {
        var child = getFirstChild();
        while (child != null) {
            if (child instanceof ElementImpl element && id.equals(element.getAttribute("id"))) {
                return element;
            }
            child = child.getNextSibling();
        }
        return null;
    }

    private List<Element> findElements(Predicate<ElementImpl> predicate) {
        var result = new ArrayList<Node>();
        for (var i = 0; i < getChildNodes().length; i++) {
            var child = getChildNodes().item(i);
            if (child instanceof ElementImpl element) {
                element.findElements(predicate, result);
            }
        }
        return result.stream().filter(Element.class::isInstance).map(Element.class::cast).toList();
    }

    protected void findElements(Predicate<ElementImpl> predicate, Collection<Node> result) {
        if (predicate.test(this)) {
            result.add(this);
        }
        var sibling = getNextSibling();
        while (sibling != null) {
            if (sibling instanceof ElementImpl element) {
                element.findElements(predicate, result);
                break;
            }
            sibling = sibling.getNextSibling();
        }
        if (getFirstChild() != null && getFirstChild() instanceof ElementImpl element) {
            element.findElements(predicate, result);
        }
    }


    private List<Element> querySelectorAll(String selector, boolean firstOnly) {
        // 1. Erstelle eine Map, um temporäre IDs auf echte Element-Objekte abzubilden.
        final String tempIdAttribute = "data-temp-id";
        Map<String, Element> elementMap = new HashMap<>();

        // Weise jedem Element im Baum eine eindeutige ID zu.
        this.findElements(e -> true).forEach(e -> {
            String uuid = UUID.randomUUID().toString();
            e.setAttribute(tempIdAttribute, uuid);
            elementMap.put(uuid, e);
        });
        // Füge auch das Wurzelelement hinzu
        String rootUuid = UUID.randomUUID().toString();
        this.setAttribute(tempIdAttribute, rootUuid);
        elementMap.put(rootUuid, this);

        // 2. Konvertiere den aktuellen Elementbaum in einen HTML-String.
        String html = this.asString();

        // 3. Parse den HTML-String mit Jsoup und führe den Selektor aus.
        Document doc = Jsoup.parseBodyFragment(html);
        List<Element> result = new ArrayList<>();

        if (firstOnly) {
            org.jsoup.nodes.Element foundJsoupElement = doc.selectFirst(selector);
            if (foundJsoupElement != null) {
                String tempId = foundJsoupElement.attr(tempIdAttribute);
                if (elementMap.containsKey(tempId)) {
                    result.add(elementMap.get(tempId));
                }
            }
        } else {
            Elements foundJsoupElements = doc.select(selector);
            for (org.jsoup.nodes.Element jsoupElement : foundJsoupElements) {
                String tempId = jsoupElement.attr(tempIdAttribute);
                if (elementMap.containsKey(tempId)) {
                    result.add(elementMap.get(tempId));
                }
            }
        }

        // 4. Bereinige die temporären Attribute aus dem echten DOM.
        elementMap.values().stream().map(ElementImpl.class::cast).forEach(e -> e.removeAttribute(tempIdAttribute));

        return result;
    }


    String asString() {
        StringBuilder builder = new StringBuilder();
        evaluateContent(builder, 0);
        return builder.toString();
    }

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
        if (getChildNodes().list().isEmpty()) {
            builder.append("/>\n");
            return;
        }
        builder.append(">\n");
        getChildNodes().stream().forEach(node -> ((NodeImpl) node).evaluateContent(builder, indent + 1));
        for (int i = 0; i < indent; i++) {
            builder.append("\t");
        }
        builder.append("</");
        builder.append(localName);
        builder.append(">\n");
    }

    public void setInnerText(String text) {
        setTextContent(text);
    }
}