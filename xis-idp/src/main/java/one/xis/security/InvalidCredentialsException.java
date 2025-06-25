package one.xis.security;

public class InvalidCredentialsException extends Exception {
    public InvalidCredentialsException(String s) {
        super(s);
    }

    public InvalidCredentialsException() {
        super();
    }
}
