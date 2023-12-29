package one.xis.server;

class JsonParseException extends RuntimeException {
    JsonParseException(String message) {
        super(message);
    }

    JsonParseException(Throwable cause) {
        super(cause);
    }

    JsonParseException(String message, Throwable cause) {
        super(message, cause);
    }
}
