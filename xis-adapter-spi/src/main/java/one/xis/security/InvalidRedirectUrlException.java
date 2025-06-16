package one.xis.security;

public class InvalidRedirectUrlException extends RuntimeException {

    public InvalidRedirectUrlException(String url) {
        super("Invalid redirect URL: " + url);
    }
}
