package one.xis.template;

public class TemplateElement extends ElementBase {
    public TemplateElement(String elementName) {
        super(elementName);
    }

    @Override
    public String toString() {
        return "<" + getElementName() + ">";
    }
}
