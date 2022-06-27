package one.xis.resource;

class NoSuchResourceException extends RuntimeException {
    NoSuchResourceException(String message) {
        super(message);
    }
}
