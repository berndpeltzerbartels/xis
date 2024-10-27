package one.xis.deserialize;

class DeserializationException extends RuntimeException {

    DeserializationException(Throwable cause) {
        super(cause);
    }

    DeserializationException(String message) {
        super(message);
    }
}
