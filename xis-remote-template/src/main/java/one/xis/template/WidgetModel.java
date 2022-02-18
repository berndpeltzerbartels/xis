package one.xis.template;

import lombok.Getter;
import lombok.experimental.Delegate;

public class WidgetModel extends ModelElement {

    @Getter
    private final String name;

    @Delegate(excludes = Excludes.class)
    private final ModelElement modelElement;

    private interface Excludes {
        void accept(WidgetModelVisitor visitor);
    }

    public WidgetModel(String name, ModelElement modelElement) {
        super(name);
        this.modelElement = modelElement;
        this.name = name;
    }

    @Override
    public void accept(WidgetModelVisitor visitor) {
        visitor.visitModel(this);
    }
}
