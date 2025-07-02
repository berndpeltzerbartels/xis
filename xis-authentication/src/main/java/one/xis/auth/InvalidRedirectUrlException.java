package one.xis.auth;

public class InvalidRedirectUrlException extends RuntimeException {

    public InvalidRedirectUrlException(String url) {
        super("Invalid redirect URL: " + url);
    }
}
