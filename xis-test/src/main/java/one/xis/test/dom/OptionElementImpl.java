package one.xis.test.dom;

import lombok.Getter;

public class OptionElementImpl extends ElementImpl implements OptionElement {

    @Getter
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

}
