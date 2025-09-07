package one.xis.test.dom;

import lombok.NonNull;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

public interface Element extends Node {

    static Element of(String html) {
        return ElementBuilder.build(html);
    }

    List<String> getAttributeNames();

    String getAttribute(String name);

    Element querySelector(String selector);

    List<Element> querySelectorAll(String selector);

    void setAttribute(String tempIdAttribute, String rootUuid);

    Map<String, String> getAttributes();

    void removeChild(Node b);

    void insertBefore(Node b, Node a);

    Element getElementById(@NonNull String id);

    NodeList getElementsByTagName(String name);


    static ElementImpl createElement(String name) {
        return switch (name) {
            case "input" -> new InputElementImpl();
            case "select" -> new SelectElementImpl();
            case "option" -> new OptionElementImpl();
            case "textarea" -> new TextareaElementImpl();
            default -> new ElementImpl(name);
        };
    }

    String getTextContent();

    boolean hasAttribute(String name);


    // Non standard methods for testing purposes

    void click();

    String getInnerHTML();

    String getInnerText();

    Node findDescendant(Predicate<Node> predicate);

    List<Node> findDescendants(Predicate<Node> predicate);

    String getTagName();

    String getLocalName();

    List<Element> getElementsByClass(String item);

    Node getNextSibling();

    NodeList getChildNodes();

    List<Element> getChildElements();

    Collection<String> getCssClasses();

    void setClassName(String className);


}
