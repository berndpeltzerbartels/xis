package one.xis.js;

import lombok.Data;

import java.util.Arrays;
import java.util.List;

@Data
public class JSArray implements JSValue {
    private final List<? extends JSValue> elements;

    public JSArray(JSValue... elements) {
        this.elements = Arrays.asList(elements);
    }

    public JSArray(List<? extends JSValue> elements) {
        this.elements = elements;
    }
}
