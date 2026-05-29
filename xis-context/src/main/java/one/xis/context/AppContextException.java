package one.xis.context;

/**
 * Runtime exception thrown while building or using a XIS application context.
 */
public class AppContextException extends RuntimeException {
    /**
     * Creates an exception with a context error message.
     */
    public AppContextException(String message) {
        super(message);
    }

    AppContextException(Throwable cause) {
        super(cause);
    }

    AppContextException(String message, Throwable cause) {
        super(message, cause);
    }
}
