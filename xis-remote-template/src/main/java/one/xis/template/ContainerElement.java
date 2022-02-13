package one.xis.template;

public class ContainerElement extends ElementBase {
    private final String containerId;
    private final String defaultWidgetId;

    public ContainerElement(String elementName, String containerId, String defaultWidgetId) {
        super(elementName);
        this.containerId = containerId;
        this.defaultWidgetId = defaultWidgetId;
    }
}
