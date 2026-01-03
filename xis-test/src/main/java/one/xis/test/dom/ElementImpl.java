package one.xis.test.dom;

import lombok.Getter;
import lombok.NonNull;
import one.xis.test.js.Event;
import one.xis.utils.lang.MethodUtils;
import one.xis.utils.lang.TypeUtils;
import org.graalvm.polyglot.Value;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;

@Getter
@SuppressWarnings("unused")
public class ElementImpl extends NodeImpl implements Element {

    // HTML-void-Tags (haben keinen End-OpeningTag; nie als <tag/> serialisieren)
    private static final Set<String> HTML_VOID = Set.of(
            "area", "base", "br", "col", "embed", "hr", "img", "input",
            "link", "meta", "param", "source", "track", "wbr"
    );

    public final String localName;
    public final DOMStringList classList = new DOMStringList();
    public final int nodeType = 1;
    private final Map<String, String> attributes = new HashMap<>();
    private final Map<String, Collection<Consumer<Object>>> eventListeners = new HashMap<>();

    public ElementImpl(String localName) {
        super(Node.ELEMENT_NODE);
        this.localName = localName;
    }

    @Override
    public void setAttribute(String name, String value) {
        if (name.equals("class")) {
            classList.clear();
            for (String item : value.split(" ")) {
                if (!item.isBlank()) {
                    classList.add(List.of(item));
                }
            }
        }
        attributes.put(name, value);
        if (getGetters().containsKey(name) && !getSetters().containsKey(name)) {
            throw new IllegalStateException("Attribute '" + name + "' is defined as a getter but not as a setter.");
        }
        if (!"setAttribute".equals(name) && getSetters().containsKey(name)) {
            var setter = getSetters().get(name);
            MethodUtils.doInvoke(this, setter, TypeUtils.convertSimple(value, setter.getParameterTypes()[0]));
        }
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
        // Getter werden als Attribute behandelt
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
        clone.classList.add(this.classList.getValues());
        getChildNodes().forEach(child -> clone.appendChild(child.cloneNode()));
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
    protected void findElements(Predicate<ElementImpl> predicate, Collection<Node> result) {
        if (predicate.test(this)) {
            result.add(this);
        }
        super.findElements(predicate, result);
    }

    @Override
    public String getTextContent() {
        return getInnerText();
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
    public void setClassName(String className) {
        this.classList.clear();
        for (String item : className.split(" ")) {
            if (!item.isBlank()) {
                this.classList.add(List.of(item));
            }
        }
    }

    @Override
    public boolean hasAttribute(String name) {
        if (name.equals("class")) {
            return classList.length > 0;
        }
        return attributes.containsKey(name);
    }

    @Override
    public void putMember(String key, Value value) {
        attributes.put(key, GraalVMUtils.convertValue(String.class, value));
        super.putMember(key, value);
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
        if (html == null || html.isEmpty()) {
            setInnerText("");
            return;
        }
        getChildNodes().clear();
        Document doc = Jsoup.parseBodyFragment(html);
        var body = doc.body();
        for (var child : body.childNodes()) {
            appendChild(convertFromJsoup(child)); // tief rekursiv
        }
        updateChildNodes();
    }

    /**
     * Tiefe Konvertierung von jsoup-Knoten in deine Node/Element-Implementierungen.
     */
    private NodeImpl convertFromJsoup(org.jsoup.nodes.Node n) {
        if (n instanceof org.jsoup.nodes.TextNode tn) {
            return new TextNodeImpl(tn.text());
        }
        if (n instanceof org.jsoup.nodes.Element e) {
            ElementImpl el = Element.createElement(e.tagName());
            // Attribute
            e.attributes().forEach(a -> el.setAttribute(a.getKey(), a.getValue()));
            // Kinder
            for (org.jsoup.nodes.Node c : e.childNodes()) {
                el.appendChild(convertFromJsoup(c));
            }
            return el;
        }
        // Andere Knotentypen (Kommentare etc.) optional: hier ignorieren
        return new TextNodeImpl(""); // oder: return null und beim Aufrufer überspringen
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
        return localName.toUpperCase();
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

    @Override
    public Object getMemberKeys() {
        var parentList = Arrays.asList((String[]) super.getMemberKeys());
        var list = new ArrayList<>(parentList);
        list.addAll(GraalVMUtils.allElementAttributeNames());
        return list.toArray(new String[list.size()]);
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
        setFirstChild(new TextNodeImpl(text));
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


    private List<Element> querySelectorAll(String selector, boolean firstOnly) {
        final String TEMP = "data-temp-id";
        Map<String, Element> elementMap = new HashMap<>();

        // 1) Marker aufbauen (alle Descendants + Root)
        this.findElements(e -> true).forEach(e -> {
            String id = UUID.randomUUID().toString();
            e.setAttribute(TEMP, id);
            elementMap.put(id, e);
        });
        String rootId = UUID.randomUUID().toString();
        this.setAttribute(TEMP, rootId);
        elementMap.put(rootId, this);

        List<Element> result = new ArrayList<>();
        try {
            String html = this.asString();

            // ⬇️ WICHTIG: Parser nach Root-Typ wählen
            org.jsoup.nodes.Document doc;
            if ("html".equalsIgnoreCase(this.getLocalName())) {
                // Volles Dokument parsen (head/body bleiben korrekt erhalten)
                doc = org.jsoup.Jsoup.parse(html, "", org.jsoup.parser.Parser.htmlParser());
            } else {
                // Fragment in <body> parsen (wie bisher)
                doc = org.jsoup.Jsoup.parseBodyFragment(html, "");
            }

            if (firstOnly) {
                org.jsoup.nodes.Element found = doc.selectFirst(selector);
                if (found != null) {
                    String id = found.attr(TEMP);
                    Element hit = elementMap.get(id);
                    if (hit != null) result.add(hit);
                }
            } else {
                for (org.jsoup.nodes.Element el : doc.select(selector)) {
                    String id = el.attr(TEMP);
                    Element hit = elementMap.get(id);
                    if (hit != null) result.add(hit);
                }
            }
        } finally {
            // 4) Marker IMMER entfernen
            elementMap.values().stream()
                    .map(ElementImpl.class::cast)
                    .forEach(e -> e.removeAttribute(TEMP));
        }
        return result;
    }


    @Override
    public String asString() {
        StringBuilder builder = new StringBuilder();
        evaluateContent(builder, 0);
        return builder.toString();
    }

    @Override
    protected void evaluateContent(StringBuilder builder, int indent) {
        for (int i = 0; i < indent; i++) builder.append('\t');
        builder.append('<').append(localName);
        attributes.forEach((key, value) -> {
            builder.append(' ')
                    .append(key)
                    .append("=\"")
                    .append(value)
                    .append('"');
        });

        boolean hasChildren = !getChildNodes().list().isEmpty();
        String ln = localName.toLowerCase();

        if (!hasChildren) {
            if (HTML_VOID.contains(ln)) {
                builder.append(">\n");                // <br>
                return;
            }
            builder.append("></").append(localName).append(">\n"); // <script></script>
            return;
        }

        builder.append(">\n");
        getChildNodes().forEach(node -> ((NodeImpl) node).evaluateContent(builder, indent + 1));
        for (int i = 0; i < indent; i++) builder.append('\t');
        builder.append("</").append(localName).append(">\n");
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append('<').append(localName);
        attributes.forEach((key, value) -> {
            builder.append(' ')
                    .append(key)
                    .append("=\"")
                    .append(value)
                    .append('"');
        });
        if (HTML_VOID.contains(localName.toLowerCase())) {
            return builder.append('>').toString();
        }
        return builder.append("/>").toString(); // nur Debug (Einzeiler)
    }

    public void setInnerText(String text) {
        setTextContent(text);
    }

}
