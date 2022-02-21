package one.xis.template;

import lombok.Data;
import lombok.Getter;
import lombok.Value;

@Getter
public class ContainerElement extends ElementBase {
    private final String containerId;
    private final String defaultWidgetId;

    public ContainerElement(String elementName, String containerId, String defaultWidgetId) {
        super(elementName);
        this.containerId = containerId;
        this.defaultWidgetId = defaultWidgetId;
    }
}
