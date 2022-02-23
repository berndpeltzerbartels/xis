package one.xis.js;

import lombok.NonNull;
import lombok.Value;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Value
public class JSFunctionCall implements JSValue, JSStatement {
    JSFunction jsFunction;
    List<JSValue> args = new ArrayList<>();

    public JSFunctionCall(@NonNull JSFunction jsFunction) {
        this.jsFunction = jsFunction;
    }

    public JSFunctionCall(@NonNull JSFunction jsFunction, JSValue... args) {
        this.jsFunction = jsFunction;
        this.args.addAll(Arrays.asList(args));
    }

    public JSFunctionCall addParam(JSValue value) {
        args.add(value);
        return this;
    }

}
