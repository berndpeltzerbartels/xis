package one.xis.context;

import lombok.RequiredArgsConstructor;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Value;

@RequiredArgsConstructor
public class JavascriptFunctionContext implements JavascriptFunction {

    private final Value value;
    private final Context context;
    
    @Override
    public void setBinding(String name, Object value) {
        context.getBindings("js").putMember(name, value);
    }

    @Override
    public Object execute(Object... args) {
        return value.execute(args);

    }

}
