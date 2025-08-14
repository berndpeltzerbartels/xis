package one.xis.test.dom;

public interface OptionElement extends Element {
    void select();

    default SelectElement findParentSelect() {
        Node parent = this.getParentNode();
        while (parent != null && !(parent instanceof SelectElement)) {
            parent = parent.getParentNode();
        }
        return (SelectElement) parent;
    }

    boolean isSelected();
}
