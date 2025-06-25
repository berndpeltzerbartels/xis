package one.xis.security;

public class InvalidTokenException extends RuntimeException {
    public InvalidTokenException(String invalidAccessTokenFormat) {
        super(invalidAccessTokenFormat);
    }

    public InvalidTokenException() {
        super("Invalid token format");
    }

    public InvalidTokenException(String message, Exception e) {
        super(message, e);
    }
}
