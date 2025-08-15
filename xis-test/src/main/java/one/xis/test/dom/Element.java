package one.xis.test.dom;

import org.jsoup.Jsoup;
import org.jsoup.select.Elements;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public interface Element extends Node {
    static Element of(String html) {
        var doc = DocumentBuilder.build(html);
        return doc.getDocumentElement();
    }
    
    void appendChild(Node node);

    String getId();

    List<String> getAttributeNames();

    String getAttribute(String name);

    Collection<String> getCssClasses();

    boolean hasChildNodes();

    List<Node> getChildList();

    String getName();

    List<Element> getChildElementsByName(String name);

    Element getChildElementByName(String name);

    List<Element> findDescendants(Predicate<Element> predicate);

    Element findDescendant(Predicate<Element> predicate);

    List<Element> getChildElements();

    List<String> getChildElementNames();

    String getTextContent();

    Element getDescendantById(String id);

    void click();

    String getInnerText();

    void findByTagName(String name, NodeList result);

    void findByClass(String cssClass, List<Element> result);

    void findElements(Predicate<Element> predicate, Collection<Element> result);

    TextNode getTextNode();

    List<Element> getChildElementsByClassName(String cssClass);

    List<Element> getDescendantElementsByClassName(String cssClass);

    String asString();

    Element querySelector(String selector);

    List<Element> querySelectorAll(String selector);

    void setInnerText(String text);

    default List<Element> querySelectorAll(String selector, boolean firstOnly) {
        // 1. Erstelle eine Map, um temporäre IDs auf echte Element-Objekte abzubilden.
        final String tempIdAttribute = "data-temp-id";
        Map<String, Element> elementMap = new HashMap<>();

        // Weise jedem Element im Baum eine eindeutige ID zu.
        this.findDescendants(e -> true).stream().map(ElementImpl.class::cast).forEach(e -> {
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
        org.jsoup.nodes.Document doc = Jsoup.parseBodyFragment(html);
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

    void setAttribute(String tempIdAttribute, String rootUuid);

    default String innerText() {
        return getChildNodes().stream()
                .filter(TextNodeIml.class::isInstance)
                .map(TextNodeIml.class::cast)
                .map(TextNode::getNodeValue)
                .filter(Objects::nonNull)
                .map(Object::toString)
                .collect(Collectors.joining());
    }

    String getLocalName();

    Node getFirstChild();

    DOMStringList getClassList();

    int getNodeType();

    NodeList getChildNodes();

    Map<String, String> getAttributes();


    Element childElement(int i);

    void removeChild(Node b);

    void insertBefore(Node b, Node a);
}
