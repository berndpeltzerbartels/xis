package one.xis.js;

import lombok.Data;

@Data
public class JSString implements JSValue {
    private final String content;
}
