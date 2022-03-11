package one.xis.template;

import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode(callSuper = false)
public class PageModel extends ElementBase implements TemplateModel {
    private final String path;
    private final TemplateElement rootElement;

    public PageModel(String path, String rootElementName, TemplateElement rootElement) {
        super(rootElementName);
        this.path = path;
        this.rootElement = rootElement;
    }
}
