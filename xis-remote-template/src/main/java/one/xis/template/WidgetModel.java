package one.xis.template;

import lombok.Getter;

@Getter
public class WidgetModel {

    private final String name;
    private final ModelNode rootNode;
    private final String path;

    public WidgetModel(String name, ModelNode rootNode) {
        this(name, rootNode, null);
    }

    public WidgetModel(String name, ModelNode rootNode, String path) {
        this.name = name;
        this.rootNode = rootNode;
        this.path = path;
    }

}
