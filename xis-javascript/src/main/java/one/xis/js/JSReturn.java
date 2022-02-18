package one.xis.js;

import lombok.Value;

@Value
public class JSReturn implements JSStatement {
    JSValue value;
}
