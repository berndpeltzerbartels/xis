package one.xis.template;

import lombok.Data;

@Data
public class PageTemplateModel implements TemplateModel {
    private final String key;
    private TemplateElement head;
    private TemplateElement body;
}
