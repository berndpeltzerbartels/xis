package one.xis.context;

/**
 * Utility methods for framework code that has to inspect the real object behind a XIS proxy.
 */
public final class ProxyTargets {

    private ProxyTargets() {
    }

    public static Object unwrap(Object value) {
        if (value instanceof XisProxy proxy) {
            return proxy.xisTarget();
        }
        return value;
    }
}
