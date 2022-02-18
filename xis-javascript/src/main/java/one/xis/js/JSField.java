package one.xis.js;


import lombok.Data;

@Data
public class JSField implements JSVariable, JSContext {
    private final JSContext context;
    private final String name;
    private JSValue value;
}
