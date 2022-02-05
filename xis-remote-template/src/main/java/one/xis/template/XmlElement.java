package one.xis.template;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Data
public class XmlElement implements Container {
    private final String tagName;
    private final Map<String, TextContent> attributes;
    private final List<TemplateElement> childElements = new ArrayList<>();

    @Override
    public List<TemplateElement> getElements() {
        return childElements;
    }

    @Override
    public void addElement(TemplateElement e) {
        childElements.add(e);
    }

    @Override
    public String toString() {
        return "<" + tagName + ">";
    }
}
