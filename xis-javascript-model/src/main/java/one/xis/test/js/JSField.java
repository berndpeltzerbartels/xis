package one.xis.test.js;

import lombok.Data;

@Data
public class JSField implements JSVariable, JSContext {
    private final JSContext context;
    private final String name;
    private JSValue value;

    @Override
    public String toString() {
        return String.format("%s(\"%s\")", getClass().getSimpleName(), name);
    }
}
