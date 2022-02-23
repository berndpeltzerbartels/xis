package one.xis.template;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;


public class TemplateElement extends ElementBase implements ChildHolder {
    @Getter
    private final List<ModelNode> children = new ArrayList<>();

    public TemplateElement(String elementName) {
        super(elementName);
    }

    void addChild(ModelNode child) {
        children.add(child);
    }

    @Override
    public String toString() {
        return "<" + getElementName() + ">";
    }
}
