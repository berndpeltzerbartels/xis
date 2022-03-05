package one.xis.template;

import lombok.Getter;

@Getter
public class WidgetModel {

    private final String name;
    private final ModelNode rootNode;
    
    public WidgetModel(String name, ModelNode rootNode) {
        this.rootNode = rootNode;
        this.name = name;
    }
}
