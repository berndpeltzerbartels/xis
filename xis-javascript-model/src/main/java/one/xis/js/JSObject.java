package one.xis.js;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class JSObject implements JSValue, JSContext {
    private final String name;
    private final JSClass jsClass;
    private final Map<String, JSValue> fields = new HashMap<>();
    private final Map<String, JSMethod> methods = new HashMap<>();

    public void addField(String name, JSValue value) {
        fields.put(name, value);
    }

    public void addMethod(String name, JSValue value) {
        fields.put(name, value);
    }
}
