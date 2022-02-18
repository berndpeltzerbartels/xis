package one.xis.template;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;


public class ModelElement extends ElementBase {
    @Getter
    private final List<ModelNode> children = new ArrayList<>();

    public ModelElement(String elementName) {
        super(elementName);
    }

    void addChild(ModelNode child) {
        children.add(child);
    }

    @Override
    public String toString() {
        return "<" + getElementName() + ">";
    }

    @Override
    public void accept(WidgetModelVisitor visitor) {
        visitor.visitElement(this);
    }
}
