package one.xis.test.dom;

import java.util.ArrayList;
import java.util.Collections;

public class DomAssert {

    public static ElementResult assertRootElement(Document document, String name) {
        assertTrue(document.rootNode.localName.equals(name), "Root element was expected to be <%s>, but it was <%s>", name, document.rootNode.localName);
        return new ElementResult(document.rootNode);
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
            assertTrue(element, e.localName.equals(name), "Expected child element at index %d to be <%s>, but it was <%s>", i, name, e.localName);
            elements.add(e);
        }
        return new ElementResults(Collections.unmodifiableList(elements));

    }

    public static ElementResult assertChildElement(Element element, String name) {
        return assertChildElements(element, name).toUniqueResult();
    }


    public static void assertNoChildElement(Element element, String name) {
        assertTrue(element, element.getChildElementsByName(name).isEmpty(), "It must not have child-element <%s>", name);
    }

    public static void assertTagName(Element element, String name) {
        assertTrue(element, element.localName.equals(name), "Expected element to have tag name '%s'", name);
    }

    public static ElementResult assertParentElement(Element element, String name) {
        assertTrue(element, element.parentNode.localName.equals(name), "Expected element to have parent tag '%s'", name);
        return new ElementResult(element.parentNode);
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
