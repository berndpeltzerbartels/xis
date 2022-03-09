package one.xis.js;

class JSValidationException extends RuntimeException {
    public JSValidationException(String message) {
        super(message);
    }

    public JSValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}
