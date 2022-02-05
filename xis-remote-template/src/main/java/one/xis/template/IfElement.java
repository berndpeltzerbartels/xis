package one.xis.template;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class IfElement implements Container {
    private final String condition;
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
        return "if(" + condition + ")";
    }
}
