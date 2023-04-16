package one.xis.test.dom;

class LoggedRuntimeException extends RuntimeException {
    LoggedRuntimeException(String message) {
        super(message);
        System.err.println(message); // TODO replace by log-api
    }
}
