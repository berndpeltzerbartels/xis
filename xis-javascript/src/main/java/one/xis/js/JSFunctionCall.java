package one.xis.js;

import lombok.Value;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Value
public class JSFunctionCall implements JSValue, JSStatement {
    JSFunction jsFunction;
    List<JSValue> args = new ArrayList<>();

    public JSFunctionCall(JSFunction jsFunction, JSValue... args) {
        this.jsFunction = jsFunction;
        this.args.addAll(Arrays.asList(args));
    }

    public JSFunctionCall withParam(JSValue value) {
        args.add(value);
        return this;
    }

}
