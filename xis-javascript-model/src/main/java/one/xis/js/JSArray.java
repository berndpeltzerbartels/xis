package one.xis.js;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;


public class JSArray implements JSValue, JSContext {
    private final List<Object> elements;

    public JSArray() {
        elements = new ArrayList<>();
    }

    public JSArray(JSValue... elements) {
        this.elements = Arrays.asList(elements);
    }


    public JSArray(List<? extends JSValue> elements) {
        this.elements = new ArrayList<>(elements);
    }

    public static JSArray arrayOfStrings(List<String> strings) {
        return new JSArray(strings.stream().map(JSString::new).collect(Collectors.toList()));
    }

    public static JSArray arrayOfValues(List<JSValue> values) {
        var array = new JSArray();
        array.elements.addAll(values);
        return array;
    }

    public void addElement(JSValue value) {
        elements.add(value);
    }

    List<JSValue> getElements() {
        return elements.stream().map(JSValue.class::cast).collect(Collectors.toList());
    }
}
