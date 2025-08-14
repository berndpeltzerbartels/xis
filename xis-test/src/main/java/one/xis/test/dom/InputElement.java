package one.xis.test.dom;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class InputElement extends Element {

    public Object value;
    public Object checked;

    InputElement() {
        super("input");
        addEventListener("click", this::handleClick);
    }

    private void handleClick(Object event) {
        if ("checkbox".equals(getAttribute("type")) || "radio".equals(getAttribute("type"))) {
            this.checked = !this.isChecked();
        }
        // TODO: Hier könnte man auch radio-buttons behandeln
    }

    @Override
    public String getAttribute(String name) {
        if ("value".equals(name)) {
            return value != null ? String.valueOf(value) : "";
        }
        return super.getAttribute(name);
    }

    public String getValue() {
        return value != null ? String.valueOf(value) : "";
    }

    public boolean isChecked() {
        if (checked == null) {
            return false;
        }
        if (checked instanceof Boolean) {
            return (Boolean) checked;
        }
        if (checked instanceof String) {
            return Boolean.parseBoolean((String) checked);
        }
        throw new IllegalStateException("Unexpected type for checked: " + checked.getClass());
    }
}