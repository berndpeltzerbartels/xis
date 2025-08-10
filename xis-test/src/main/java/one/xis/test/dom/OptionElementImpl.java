package one.xis.test.dom;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OptionElementImpl extends ElementImpl implements OptionElement {

    public boolean selected;
    private String value;

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

    @SuppressWarnings("unused")
    void setSelected(boolean selected) {
        this.selected = selected;
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
