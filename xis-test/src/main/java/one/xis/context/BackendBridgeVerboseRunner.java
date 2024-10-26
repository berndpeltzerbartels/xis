package one.xis.context;

import org.tinylog.Logger;

import java.util.function.Function;
import java.util.function.Supplier;

class BackendBridgeVerboseRunner {
    static <R> R run(Supplier<R> fct) {
        try {
            return fct.get();
        } catch (Exception e) {
            Logger.error(e, "Backendbridge failed");
            throw e; // Bad code, but otherwise stack-trace is lost
        }
    }

    static <P, R> R run(Function<P, R> fct, P param) {
        try {
            return fct.apply(param);
        } catch (Exception e) {
            Logger.error(e, "Backendbridge failed");
            throw e; // Bad code, but otherwise stack-trace is lost
        }
    }
}
