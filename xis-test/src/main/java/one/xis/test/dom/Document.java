package one.xis.test.dom;

import one.xis.utils.io.IOUtils;

import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;

/**
 * Document used as mock for html-documents. Sorrily {@link org.w3c.dom.Element} is using
 * getters instead of fields. So it can not be used for html-testing.
 */
@SuppressWarnings("unused")
public interface Document {

    Element querySelector(String selector);

    List<Element> querySelectorAll(String selector);

    String getTextContent();

    Element getBody();

    Element getHead();

    String getTitle();

    NodeList getElementsByTagName(String name);

    Element getElementById(String id);

    InputElement getInputElementById(String id);

    List<Element> getElementsByClass(String cssClass);

    Element getElementByTagName(String name);

    static Document fromResource(String classPathResource) {
        return of(IOUtils.getResourceAsString(classPathResource));
    }

    Element findElement(Predicate<Element> predicate);

    String asString();

    // TODO test
    Collection<Element> findElements(Predicate<Element> predicate);

    static Document of(String html) {
        return DocumentBuilder.build(html);
    }


    private InputElement createInputElement(String type) {
        return new InputElementImpl();
    }

    Location getLocation();

    Element getDocumentElement();
}
