package one.xis.http;

public enum ContentType {
    JSON("application/json"),
    FORM_URLENCODED("application/x-www-form-urlencoded"),
    TEXT_PLAIN("text/plain"),
    TEXT_HTML("text/html"),
    APPLICATION_OCTET_STREAM("application/octet-stream");

    private final String value;

    ContentType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
