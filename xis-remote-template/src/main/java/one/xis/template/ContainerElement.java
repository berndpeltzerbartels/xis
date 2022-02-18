package one.xis.template;

import lombok.Value;

@Value
public class ContainerElement extends ElementBase {
    String containerId;
    String defaultWidgetId;

    public ContainerElement(String elementName, String containerId, String defaultWidgetId) {
        super(elementName);
        this.containerId = containerId;
        this.defaultWidgetId = defaultWidgetId;
    }

    @Override
    public void accept(WidgetModelVisitor visitor) {
        visitor.visitContainer(this);
    }
}
