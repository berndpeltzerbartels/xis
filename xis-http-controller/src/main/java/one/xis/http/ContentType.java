package one.xis.http;

/**
 * Content types understood by the plain HTTP controller layer.
 *
 * <p>Use these values with {@link Produces}, {@link ResponseEntity}, and
 * adapter responses when the content type should be explicit.</p>
 */
public enum ContentType {
    JSON("application/json"),
    JSON_UTF8("application/json; charset=utf-8"),
    FORM_URLENCODED("application/x-www-form-urlencoded"),
    MULTIPART_FORM_DATA("multipart/form-data"),
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

    /**
     * Resolves a header value such as {@code application/json; charset=utf-8}
     * to a known content type.
     *
     * @param contentType raw HTTP header value
     * @return matching content type, or {@code null} when the input is {@code null}
     */
    public static ContentType fromValue(String contentType) {
        if (contentType == null) {
            return null;
        }
        for (ContentType type : ContentType.values()) {
            if (contentType.toLowerCase().startsWith(type.value.toLowerCase())) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown content type: " + contentType);
    }

    public String getValue() {
        return value;
    }

}
