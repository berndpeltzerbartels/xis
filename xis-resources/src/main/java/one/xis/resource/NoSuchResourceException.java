package one.xis.resource;

public class NoSuchResourceException extends RuntimeException {
    NoSuchResourceException(String message) {
        super(message);
    }
}
