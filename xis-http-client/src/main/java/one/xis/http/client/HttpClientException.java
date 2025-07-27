package one.xis.http.client;

public class HttpClientException extends Exception {
    public HttpClientException(String message, Exception e) {
        super(message, e);
    }

    public HttpClientException(String message) {
        super(message);
    }
}
