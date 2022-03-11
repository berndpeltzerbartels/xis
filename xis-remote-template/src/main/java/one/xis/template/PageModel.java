package one.xis.template;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@EqualsAndHashCode(callSuper = false)
public class PageModel implements TemplateModel, ChildHolder, ElementWithAttributes {
    private final String path;
    private final String elementName;
    private final TemplateElement rootElement;
    private final Map<String, MutableAttribute> mutableAttributes = new HashMap<>();
    private final Map<String, String> staticAttributes = new HashMap<>();

    @Override
    public void addChild(ModelNode child) {
        rootElement.addChild(child);
    }

    @Override
    public List<ModelNode> getChildren() {
        return rootElement.getChildren();
    }

}
