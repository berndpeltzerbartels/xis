package one.xis.security;

public class InvalidCredentialsException extends AuthenticationException {
    public InvalidCredentialsException(String s) {
        super(s);
    }

    public InvalidCredentialsException() {
        super();
    }
}
