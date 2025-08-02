package one.xis.auth;

public class AuthorizationException extends AuthenticationException {
    public AuthorizationException() {
    }

    public AuthorizationException(Throwable cause) {
        super(cause);
    }

    public AuthorizationException(String s) {
        super(s);
    }

    public AuthorizationException(String s, Throwable cause) {
        super(s, cause);
    }
}
