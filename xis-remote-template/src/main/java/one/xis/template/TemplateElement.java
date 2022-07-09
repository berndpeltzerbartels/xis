package one.xis.template;

/**
 * Representation of an element/tag of the html-template.
 */
public class TemplateElement extends ElementBase {
    public TemplateElement(String elementName) {
        super(elementName);
    }

    @Override
    public String toString() {
        return "<" + getElementName() + ">";
    }
}
