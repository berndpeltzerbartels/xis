package one.xis.js;

import lombok.Value;

import java.util.HashMap;
import java.util.Map;

@Value
public class JSJsonValue implements JSValue {
    Map<String, JSValue> fields = new HashMap<>();

    public JSJsonValue addField(String name, JSValue value) {
        fields.put(name, value);
        return this;
    }
}
