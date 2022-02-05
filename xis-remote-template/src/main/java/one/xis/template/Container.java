package one.xis.template;

import java.util.List;

public interface Container extends TemplateElement {
    List<? extends TemplateElement> getElements();

    void addElement(TemplateElement e);
}
