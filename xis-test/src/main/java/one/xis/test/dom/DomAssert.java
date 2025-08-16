package one.xis.test.dom;

import java.util.ArrayList;
import java.util.Collections;

public class DomAssert {

    public static ElementResult assertAndGetRootElement(Document document, String name) {
        var documentImpl = (DocumentImpl) document;
        assertTrue(documentImpl.getDocumentElement().getLocalName().equals(name), "Root element was expected to be <%s>, but it was <%s>", name, documentImpl.getDocumentElement().getLocalName());
        return new ElementResult(documentImpl.getDocumentElement());
    }


    public static ElementResults assertChildElements(Element element, String... names) {
        var childElements = new ArrayList<>(element.getChildElements());
        if (childElements.size() < names.length) {
            switch (childElements.size()) {
                case 0:
                    throw new DomAssertionException(element, "Expected child elements '%s', but no child elements are present", String.join(", ", names));
                case 1:
                    throw new DomAssertionException(element, "Expected child elements '%s', but only one child is present", String.join(", ", names));
                default:
                    throw new DomAssertionException(element, "Expected child elements '%s', but only %d children are present", String.join(", ", names), childElements.size());
            }

        }
        var elements = new ArrayList<Element>();
        for (int i = 0; i < names.length; i++) {
            var name = names[i];
            var e = childElements.get(i);
            assertTrue(element, e.getLocalName().equals(name), "Expected child element at index %d to be <%s>, but it was <%s>", i, name, e.getLocalName());
            elements.add(e);
        }
        return new ElementResults(Collections.unmodifiableList(elements));

    }

    public static ElementResult assertAndGetChildElement(Element element, String name) {
        return assertChildElements(element, name).toUniqueResult();
    }


    public static void assertNoChildElement(Element element, String name) {
        assertTrue(element, element.getElementsByTagName(name).isEmpty(), "It must not have child-element <%s>", name);
    }

    public static void assertTagName(Element element, String name) {
        assertTrue(element, element.getLocalName().equals(name), "Expected element to have tag name '%s'", name);
    }

    public static ElementResult assertAndGetParentElement(Element element, String name) {
        assertTrue(element, ((ElementImpl) element.getParentNode()).getLocalName().equals(name), "Expected element to have parent tag '%s'", name);
        return new ElementResult((ElementImpl) element.getParentNode());
    }

    static void assertTrue(boolean result, String errorMessage, Object... args) {
        if (!result) {
            throw new DomAssertionException(errorMessage, args);
        }
    }

    static void assertTrue(Element element, boolean result, String errorMessage, Object... args) {
        if (!result) {
            throw new DomAssertionException(element, errorMessage, args);
        }
    }

}
