package one.xis.test.js;

import lombok.Value;

import java.util.HashMap;
import java.util.Map;

@Value
public class JSObject implements JSValue {
    Map<String, JSValue> fields = new HashMap<>();

    public JSObject addField(String name, JSValue value) {
        fields.put(name, value);
        return this;
    }
}
