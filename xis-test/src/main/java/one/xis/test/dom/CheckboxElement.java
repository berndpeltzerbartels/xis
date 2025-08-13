package one.xis.test.dom;

import lombok.Getter;

public class CheckboxElement extends InputElement {
    @Getter
    public boolean checked;

    public CheckboxElement() {
        addEventListener("click", this::check);
    }

    public void check(Object event) {
        checked = true;
    }
}
