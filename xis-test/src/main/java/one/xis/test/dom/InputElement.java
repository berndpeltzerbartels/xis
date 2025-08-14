package one.xis.test.dom;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class InputElement extends Element {
    
    public Object value;

    InputElement() {
        super("input");
    }


    public void typeInputAndBlur(String input) {
        focus(this);
        if (input == null) {
            input = "";
        }
        if (!input.equals(value)) {
            value = input;
            fireEvent("change");
        }
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
}
