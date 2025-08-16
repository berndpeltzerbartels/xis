package one.xis.test.dom;

import lombok.Getter;

@Getter
public class OptionElementImpl extends ElementImpl implements OptionElement {

    public boolean selected;

    public OptionElementImpl() {
        super("option");
    }

    @Override
    public void select() {
        selected = true;
        var select = findParentSelect();
        if (select != null) {
            select.updateSelectionState(this);
        }
    }

    @Override
    public void deselect() {
        selected = false;
        var select = findParentSelect();
        if (select != null) {
            select.updateSelectionState(this);
        }
    }

    SelectElementImpl findParentSelect() {
        var parent = this.getParentNode();
        while (parent != null && !(parent instanceof SelectElementImpl)) {
            parent = parent.getParentNode();
        }
        return (SelectElementImpl) parent;
    }

}
