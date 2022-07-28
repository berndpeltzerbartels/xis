package one.xis.js;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class JSBoolean implements JSValue {
    private final boolean value;

    @Override
    public String toString() {
        return Boolean.toString(value);
    }
}
