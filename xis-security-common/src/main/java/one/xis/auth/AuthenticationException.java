package one.xis.auth;

public class AuthenticationException extends RuntimeException {
    public AuthenticationException(String s) {
        super(s);
    }

    public AuthenticationException(String s, Throwable cause) {
        super(s, cause);
    }

    public AuthenticationException(Throwable cause) {
        super(cause);
    }

    public AuthenticationException() {
        super();
    }
}
