package one.xis.js;

import lombok.Data;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class JSArray implements JSValue {
    private final List<? extends JSValue> elements;

    public JSArray(JSValue... elements) {
        this.elements = Arrays.asList(elements);
    }


    public JSArray(List<? extends JSValue> elements) {
        this.elements = elements;
    }

    public static JSArray arrayOfStrings(List<String> strings) {
        return new JSArray(strings.stream().map(JSString::new).collect(Collectors.toList()));
    }
}
