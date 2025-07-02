package one.xis.auth;

public class InvalidCredentialsException extends Exception {
    public InvalidCredentialsException(String s) {
        super(s);
    }

    public InvalidCredentialsException() {
        super();
    }
}
