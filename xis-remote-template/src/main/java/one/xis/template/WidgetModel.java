package one.xis.template;

import lombok.Getter;
import lombok.experimental.Delegate;

public class WidgetModel extends TemplateElement {

    @Getter
    private final String name;

    @Delegate
    private final TemplateElement element;


    public WidgetModel(String name, TemplateElement element) {
        super(name);
        this.element = element;
        this.name = name;
    }
}
