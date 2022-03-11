package one.xis.template;

import lombok.Data;

@Data
public class WidgetModel implements TemplateModel {

    private final String name;
    private final ModelNode rootNode;

}
