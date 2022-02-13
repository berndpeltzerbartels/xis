package one.xis.template;

import lombok.Getter;

public class WidgetModel extends ModelElement {

    @Getter
    private final String name;

    WidgetModel(String name, String elementName) {
        super(elementName);
        this.name = name;
    }

    WidgetModel(String name, ModelElement modelElement) {
        super(modelElement.getElementName());
        this.name = name;
    }
}
