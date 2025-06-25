package one.xis.http;

public class HttpClientException extends Exception {
    public HttpClientException(String message, Exception e) {
        super(message, e);
    }

    public HttpClientException(String message) {
        super(message);
    }
}
