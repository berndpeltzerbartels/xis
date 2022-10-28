package one.xis.template;

import lombok.Data;

@Data
public class WidgetTemplateModel implements TemplateModel {

    private final String widgetJavascriptClassName;
    private final ModelNode rootNode;

}
