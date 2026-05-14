package one.xis.http;

/**
 * Signals that an SSE payload could not be written to the client connection.
 */
public class SseSendFailedException extends RuntimeException {

    public SseSendFailedException(String message) {
        super(message);
    }

    public SseSendFailedException(String message, Throwable cause) {
        super(message, cause);
    }
}
