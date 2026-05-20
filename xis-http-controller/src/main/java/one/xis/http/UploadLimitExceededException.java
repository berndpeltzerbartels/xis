package one.xis.http;

public class UploadLimitExceededException extends RuntimeException {

    public UploadLimitExceededException(String message) {
        super(message);
    }
}
