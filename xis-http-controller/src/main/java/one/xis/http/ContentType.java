package one.xis.http;

public enum ContentType {
    JSON_UTF8("application/json; charset=utf-8"),
    FORM_URLENCODED("application/x-www-form-urlencoded"),
    TEXT_PLAIN("text/plain"),
    TEXT_HTML_UTF8("text/html; charset=utf-8"),
    APPLICATION_OCTET_STREAM("application/octet-stream"),
    PDF("application/pdf"),
    JAVASCRIPT("application/javascript"),
    CSS("text/css"),
    XML("application/xml"),
    JPEG("image/jpeg"),
    PNG("image/png"),
    GIF("image/gif"),
    SVG("image/svg+xml"),
    ZIP("application/zip");

    private final String value;

    ContentType(String value) {
        this.value = value;
    }

    public static ContentType fromValue(String contentType) {
        for (ContentType type : ContentType.values()) {
            if (type.value.equalsIgnoreCase(contentType)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown content type: " + contentType);
    }

    public String getValue() {
        return value;
    }

}
