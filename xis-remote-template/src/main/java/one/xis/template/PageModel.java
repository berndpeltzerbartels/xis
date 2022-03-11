package one.xis.template;

import lombok.Data;

@Data
public class PageModel implements TemplateModel {
    private final String path;
    private TemplateElement head;
    private TemplateElement body;
}
