package one.xis.http.client;

public record HttpResponse(String content, int statusCode) {

    public boolean isSuccess() {
        return statusCode >= 200 && statusCode < 300;
    }

    public boolean isError() {
        return !isSuccess();
    }

    public String getContent() {
        return content;
    }

    public int getStatusCode() {
        return statusCode;
    }
}
