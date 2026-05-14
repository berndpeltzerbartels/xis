package one.xis.context;

import org.graalvm.polyglot.Value;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

public final class PolyglotPromises {

    private PolyglotPromises() {
    }

    public static Object await(Object value) {
        if (!(value instanceof Value polyglotValue) || !isPromise(polyglotValue)) {
            return value;
        }

        var completed = new AtomicBoolean(false);
        var result = new AtomicReference<>();
        var error = new AtomicReference<>();

        polyglotValue.invokeMember("then",
                (Consumer<Object>) resolved -> {
                    result.set(resolved);
                    completed.set(true);
                },
                (Consumer<Object>) rejected -> {
                    error.set(rejected);
                    completed.set(true);
                });

        if (!completed.get()) {
            throw new IllegalStateException("JavaScript Promise did not settle synchronously in the integration-test runtime");
        }
        if (error.get() != null) {
            throw new RuntimeException("JavaScript Promise was rejected: " + error.get());
        }
        return result.get();
    }

    private static boolean isPromise(Value value) {
        return value.hasMember("then") && value.getMember("then").canExecute();
    }
}
