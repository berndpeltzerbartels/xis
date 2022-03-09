package one.xis.js;

import lombok.Value;

/**
 * A javascript string, e.g. '123'
 */
@Value
public class JSString implements JSValue {
    String content;

    @Override
    public String toString() {
        return content;
    }
}
