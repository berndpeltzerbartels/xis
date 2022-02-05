package one.xis.template;

import lombok.Data;

import java.util.List;

@Data
public class TemplateModel implements Container {

    private final TemplateElement root;
    private final String name;
    
    @Override
    public List<? extends TemplateElement> getElements() {
        return List.of(root);
    }

    @Override
    public void addElement(TemplateElement e) {
        throw new AbstractMethodError();
    }
}
