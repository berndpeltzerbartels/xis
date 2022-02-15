package one.xis.template;

import lombok.Getter;
import lombok.experimental.Delegate;

public class WidgetModel {

    @Getter
    private final String name;

    @Delegate
    private final ModelElement modelElement;

    WidgetModel(String name, ModelElement modelElement) {
        this.modelElement = modelElement;
        this.name = name;
    }
}
