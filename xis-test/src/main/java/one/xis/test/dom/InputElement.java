package one.xis.test.dom;

import lombok.Getter;

public class InputElement extends Element {

    @Getter
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
