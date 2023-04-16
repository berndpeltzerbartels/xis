package one.xis.test.dom;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import one.xis.utils.lang.StringUtils;

import static one.xis.test.dom.DomAssert.assertTrue;

@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class ElementResult {

    @Getter
    private final Element element;

    public ElementResult assertChildElements(String... names) {
        DomAssert.assertChildElements(element, names);
        return this;
    }

    public ElementResults assertAndGetChildElements(String... names) {
        return DomAssert.assertChildElements(element, names);
    }

    public ElementResult assertAndGetChildElement(String name) {
        return DomAssert.assertAndGetChildElement(element, name);
    }

    public ElementResult assertNoChildElement(String name) {
        assertTrue(element, element.getChildElementsByName(name).isEmpty(), "It must not have child-element <%s>", name);
        return this;
    }

    public ElementResult assertAttribute(String name) {
        assertTrue(element, element.getAttribute(name) != null, "It should have attribute '%s'", name);
        return this;
    }

    public ElementResult assertAttribute(String name, String value) {
        assertTrue(element, value.equals(element.getAttribute(name)), "It should have attribute '%s'='%s'", name, value);
        return this;
    }

    public ElementResult assertTextContent(String content) {
        assertTrue(element, content.equals(element.getTextContent()), "Expected text-content to be '%s', bit it was '%s'", content, element.getTextContent());
        return this;
    }


    public ElementResult assertTrimmedContent(String content) {
        assertTrue(element, content.equals(StringUtils.trimNullSafe(element.getTextContent())), "Expected text-content to be '%s', bit it was '%s'", content, element.getTextContent());
        return this;
    }

    public ElementResult assertNoChildElements() {
        assertTrue(element, element.getChildElements().isEmpty(), "Expected element to have no child elements");
        return this;
    }

    public ElementResult assertTagName(String name) {
        assertTrue(element, element.getChildElements().isEmpty(), "Expected element to have tag name '%s'", name);
        return this;
    }

    public ElementResult assertAndGetParentElement(String name) {
        return DomAssert.assertAndGetParentElement(element, name);
    }
}
