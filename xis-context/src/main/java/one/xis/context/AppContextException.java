package one.xis.context;

public class AppContextException extends RuntimeException {
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
