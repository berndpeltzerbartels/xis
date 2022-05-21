package one.xis.context;

class AppContextException extends RuntimeException {
    AppContextException(String message) {
        super(message);
    }

    AppContextException(Throwable cause) {
        super(cause);
    }
}
