package one.xis.ws;

public class UnknownSessionException extends RuntimeException {
    public UnknownSessionException(String sessionId) {
        super("unknown session: " + sessionId);
    }
}
