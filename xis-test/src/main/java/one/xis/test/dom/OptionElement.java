package one.xis.test.dom;

import lombok.Getter;

public class OptionElement extends Element {

    @Getter
    public boolean selected;

    public OptionElement() {
        super("option");
    }
    
    public void select() {
        selected = true;
        var select = findParentSelect();
        if (select != null) {
            select.updateSelectionState(this);
        }
    }

    private SelectElement findParentSelect() {
        Node parent = this.parentNode;
        while (parent != null && !(parent instanceof SelectElement)) {
            parent = parent.parentNode;
        }
        return (SelectElement) parent;
    }
}
