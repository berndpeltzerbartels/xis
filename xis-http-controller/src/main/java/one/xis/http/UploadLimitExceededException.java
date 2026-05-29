package one.xis.http;

/**
 * Thrown when a multipart upload exceeds the configured upload limits.
 */
public class UploadLimitExceededException extends RuntimeException {

    /**
     * Creates an exception with a human-readable upload limit message.
     */
    public UploadLimitExceededException(String message) {
        super(message);
    }
}
