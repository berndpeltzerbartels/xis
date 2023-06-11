package one.xis.context;

import lombok.NonNull;

import javax.script.CompiledScript;
import java.util.function.Function;

/**
 * Hack fixing the problem that when using chrome debugger,
 * function representation's type is different to that one from normal execution.
 */
public class JavascriptFunctionCompiled implements JavascriptFunction {
    private final Function<Object, Object> functionObj;
    private final CompiledScript compiledScript;

    public JavascriptFunctionCompiled(@NonNull Function<Object, Object> functionObj, @NonNull CompiledScript compiledScript) {
        this.compiledScript = compiledScript;
        this.functionObj = functionObj;
    }

    public void setBinding(String name, Object value) {
        compiledScript.getEngine().put(name, value);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Object execute(Object... args) {
        return functionObj.apply(args);
    }


}
