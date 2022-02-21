package one.xis.js;

import lombok.Value;

/**
 * A variable-name or a number.
 */
@Value
public class JSConstant implements JSValue {
    String content;
}