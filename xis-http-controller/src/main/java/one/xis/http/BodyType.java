package one.xis.http;

/**
 * Request body decoding modes for {@link RequestBody}.
 */
public enum BodyType {
    /**
     * Decode the body as JSON and map it to the annotated parameter type.
     */
    JSON,
    /**
     * Decode {@code application/x-www-form-urlencoded} data and map form fields
     * to the annotated parameter type.
     */
    FORM_URLENCODED, // Formerly FORM_DATA
    /**
     * Decode the body as text.
     */
    TEXT,
    /**
     * Pass the raw request bytes to the annotated parameter.
     */
    BINARY;
}
