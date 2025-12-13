package one.xis.test.dom;

import java.util.List;

/**
 * Document used as mock for html-documents. Sorrily {@link org.w3c.dom.Element} is using
 * getters instead of fields. So it can not be used for html-testing.
 */
@SuppressWarnings("unused")
public interface Document {

    Element querySelector(String selector);

    List<Element> querySelectorAll(String selector);

    TextNode createTextNode(String content);

    NodeList getElementsByTagName(String name);

    Element getElementById(String id);

    <E extends Element> E getElementById(String id, Class<E> elementClass);

    static Document of(String html) {
        return DocumentBuilder.build(html);
    }

    // Non standard methods for testing purposes
    InputElement getInputElementById(String id);

    Element getElementByTagName(String tagName);

    String asString();

    List<Element> getElementsByClass(String item);

    Element getDocumentElement();

    String getTextContent();

    String getTitle();

    DocumentFragmentImpl createDocumentFragment();

    Element createElement(String name);

    Location getLocation();
    
}
