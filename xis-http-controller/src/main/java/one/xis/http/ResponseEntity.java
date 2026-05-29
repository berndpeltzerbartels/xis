package one.xis.http;

import lombok.Getter;

import java.time.Duration;
import java.util.*;

/**
 * Return value wrapper for plain HTTP controller methods.
 *
 * <p>Use this when a controller needs to set an HTTP status code, headers,
 * cookies, redirects, or a body explicitly. Returning a plain object is enough
 * for simple {@code 200 OK} JSON responses.</p>
 *
 * @param <T> body type
 */
public class ResponseEntity<T> {

    @Getter
    private T body;
    private long lastModified;

    @Getter
    private final int statusCode;
    private final Map<String, List<String>> headers = new HashMap<>();


    public ResponseEntity(T body, int statusCode) {
        this.body = body;
        this.statusCode = statusCode;
    }

    /**
     * Creates a response without a body.
     */
    public ResponseEntity(int statusCode) {
        this(null, statusCode);
    }

    /**
     * Creates a {@code 200 OK} response.
     */
    public static <T> ResponseEntity<T> ok(T body) {
        return new ResponseEntity<>(body, 200);
    }

    /**
     * Creates a {@code 201 Created} response.
     */
    public static <T> ResponseEntity<T> created(T body) {
        return new ResponseEntity<>(body, 201);
    }

    /**
     * Creates a {@code 204 No Content} response.
     */
    public static <T> ResponseEntity<T> noContent() {
        return new ResponseEntity<>(204);
    }

    /**
     * Creates a {@code 400 Bad Request} response.
     */
    public static <T> ResponseEntity<T> badRequest() {
        return new ResponseEntity<>(400);
    }

    /**
     * Creates a {@code 404 Not Found} response.
     */
    public static <T> ResponseEntity<T> notFound() {
        return new ResponseEntity<>(404);
    }

    /**
     * Creates a response with a custom status code and no body.
     */
    public static <T> ResponseEntity<T> status(int statusCode) {
        return new ResponseEntity<>(null, statusCode);
    }

    /**
     * Creates a response with a custom status code and body.
     */
    public static <T> ResponseEntity<T> status(int statusCode, T body) {
        return new ResponseEntity<>(body, statusCode);
    }

    /**
     * Sets the last-modified timestamp in epoch milliseconds.
     */
    public ResponseEntity<T> lastModified(long epochMilli) {
        this.lastModified = epochMilli;
        return this;
    }

    /**
     * Adds a response header value.
     */
    public ResponseEntity<T> addHeader(String name, String value) {
        headers.computeIfAbsent(name.toUpperCase(), k -> new ArrayList<>()).add(value);
        return this;
    }

    /**
     * Returns the first header value for the given name.
     */
    public String getHeader(String name) {
        List<String> values = headers.get(name.toUpperCase());
        return values != null && !values.isEmpty() ? values.get(0) : null;
    }

    /**
     * Returns all header values for the given name.
     */
    public List<String> getHeaders(String name) {
        List<String> values = headers.get(name.toUpperCase());
        return values != null ? Collections.unmodifiableList(values) : Collections.emptyList();
    }


    /**
     * Returns the response header names that have been set on this response.
     */
    public Collection<String> getHeaderNames() {
        return headers.keySet();
    }

    /**
     * Creates a {@code 302} redirect response.
     */
    public static ResponseEntity<?> redirect(String location) {
        ResponseEntity<?> response = new ResponseEntity<>(302);
        response.addHeader("Location", location);
        return response;
    }

    /**
     * Adds a cookie header intended for authentication tokens.
     * <p>
     * The cookie is created with the {@code Secure} attribute here because a
     * {@code ResponseEntity} does not know the transport scheme. The concrete
     * response writer removes that attribute only for plain HTTP requests.
     * <p>
     * This extra step exists for Safari: when a local XIS application runs on
     * {@code http://localhost}, Safari silently ignores token cookies that carry
     * {@code Secure}. The login endpoint then creates valid tokens, redirects to
     * the target page, and the next request still looks anonymous. On HTTPS,
     * including production systems behind a correctly configured reverse proxy,
     * the {@code Secure} attribute is preserved. The development fallback is
     * therefore not a production security downgrade.
     */
    public ResponseEntity<T> addSecureCookie(String name, String value, Long maxAgeSeconds) {
        StringJoiner cookieValue = new StringJoiner("; ");
        cookieValue.add(name + "=" + value);
        cookieValue.add("HttpOnly");
        cookieValue.add("Secure");
        cookieValue.add("SameSite=Lax");
        cookieValue.add("Max-Age=" + maxAgeSeconds);
        cookieValue.add("Path=/");

        headers.computeIfAbsent("SET-COOKIE", k -> new ArrayList<>()).add(cookieValue.toString());

        return this;
    }

    public ResponseEntity<T> addSecureCookie(String name, String value, Duration maxAge) {
        return addSecureCookie(name, value, maxAge.toSeconds());
    }

    /**
     * Adds an HTTP-only cookie.
     */
    public ResponseEntity<T> addCookie(String name, String value, Duration maxAge) {
        StringJoiner cookieValue = new StringJoiner("; ");
        cookieValue.add(name + "=" + value);
        cookieValue.add("HttpOnly");
        cookieValue.add("SameSite=Lax");
        cookieValue.add("Max-Age=" + maxAge.getSeconds());
        cookieValue.add("Path=/");

        headers.computeIfAbsent("SET-COOKIE", k -> new ArrayList<>()).add(cookieValue.toString());

        return this;
    }

    /**
     * Replaces the response body.
     */
    public ResponseEntity<T> body(T body) {
        this.body = body;
        return this;
    }

    /**
     * Clears the response body while preserving the generic body type.
     */
    public ResponseEntity<T> emptyBody(Class<T> bodyType) {
        this.body = null;
        return this;
    }

}
