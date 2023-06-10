package one.xis.context;

import org.graalvm.polyglot.Value;

import java.util.function.Function;

/**
 * Hack fixing the problem that when using chrome debugger,
 * function representation's type is different to that one from normal execution.
 */
class GraalVMFunction {
    private Object functionObj;

    GraalVMFunction(Object result) {
        this.functionObj = result;
    }

    @SuppressWarnings("unchecked")
    Object execute(String... args) {
        if (functionObj instanceof Value) {
            return ((Value) functionObj).execute(args); // with chrome debugger
        } else if (functionObj instanceof Function) {
            return ((Function<String[], Object>) functionObj).apply(args);
        }
        throw new IllegalStateException();
    }


}
