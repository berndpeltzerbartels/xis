package one.xis.context;

import org.tinylog.Logger;

import java.util.function.Function;
import java.util.function.Supplier;

class BackendBridgeVerboseRunner {
    static <R> R run(Supplier<R> fct) {
        return executeAndLogOnError(fct);
    }

    static <P, R> R run(Function<P, R> fct, P param) {
        return executeAndLogOnError(() -> fct.apply(param));
    }

    private static <R> R executeAndLogOnError(Supplier<R> supplier) {
        try {
            return supplier.get();
        } catch (Exception e) {
            Logger.error(e, "Backendbridge failed");
            // Das erneute Werfen der Exception erhält den ursprünglichen Stack-Trace.
            throw e;
        }
    }
}