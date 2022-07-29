package one.xis.template;

import lombok.Data;

@Data
public class PageTemplateModel implements TemplateModel {
    private final String key;
    private TemplateHeadElement head;
    private TemplateBodyElement body;
}
