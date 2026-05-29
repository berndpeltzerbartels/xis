package one.xis.http;

/**
 * HTTP methods supported by the plain HTTP controller router.
 */
public enum HttpMethod {
    GET, POST, PUT, PATCH, DELETE, HEAD, OPTIONS, TRACE;

    /**
     * Parses a request method name.
     */
    public static HttpMethod fromString(String method) {
        try {
            return HttpMethod.valueOf(method.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid HTTP method: " + method, e);
        }
    }
}
