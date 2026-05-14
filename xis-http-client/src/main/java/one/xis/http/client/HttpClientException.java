package one.xis.http.client;

import lombok.Getter;

@Getter
public class HttpClientException extends Exception {

    private final int statusCode;

    public HttpClientException(String message, Exception e, int statusCode) {
        super(message, e);
        this.statusCode = statusCode;
    }

    public HttpClientException(String message, int statusCode) {
        super(message);
        this.statusCode = statusCode;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("HttpClientException{");
        if (statusCode > 0) {
            sb.append("statusCode=").append(statusCode).append(", ");
        }
        sb.append("message='").append(getMessage()).append('\'');
        sb.append(", cause=").append(getCause());
        sb.append('}');
        return sb.toString();
    }

    @Override
    public String getMessage() {
        String message = super.getMessage();
        if (message == null || message.isEmpty()) {
            return "An error occurred in the HTTP client.";
        }
        if (statusCode <= 0) {
            return message;
        }
        return "HTTP " + statusCode + ": " + message;
    }
}
