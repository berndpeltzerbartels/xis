package one.xis.test.dom;

import lombok.Getter;
import lombok.Setter;

public class InputElement extends Element {

    @Getter
    @Setter
    public String value;

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
}
