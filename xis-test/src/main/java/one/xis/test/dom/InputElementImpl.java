package one.xis.test.dom;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class InputElementImpl extends ElementImpl implements InputElement {

    private String value;
    private boolean checked;
    private String type;

    public InputElementImpl() {
        super("input");
    }

    protected InputElementImpl(String name) {
        super(name);
    }

    private void onClick() {
        this.checked = !this.checked;
    }


    @Override
    public void setAttribute(String name, String value) {
        if (name.equals("type")) {
            if ("checkbox".equals(value) || "radio".equals(value)) {
                this.checked = false; // Reset checked state for checkbox/radio inputs
                addEventListener("click", event -> {
                    if ("checkbox".equals(type) || "radio".equals(type)) {
                        onClick();
                    }
                });
            }

        }
        super.setAttribute(name, value);
    }
}
