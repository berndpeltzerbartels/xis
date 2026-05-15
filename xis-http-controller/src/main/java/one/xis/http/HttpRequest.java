package one.xis.http;

import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Map;

/**
 * Represents an HTTP request.
 * Provides methods to access the request path and the real path.
 */
public interface HttpRequest {

    /**
     * Gets the path of the request. It does not contain a context path
     * or any query parameters.
     *
     * @return the path as a String.
     */
    String getPath();

    /**
     * Gets the real path of the request. This includes the context path
     * and any additional segments that may be part of the request.
     *
     * @return the real path as a String.
     */
    //  String getRealPath();

    Map<String, String> getQueryParameters();

    byte[] getBody();

    default String getBodyAsString() {
        return new String(getBody(), StandardCharsets.UTF_8);
    }

    ContentType getContentType();

    int getContentLength();

    //Collection<String> getHeaderNames();

    String getHeader(String name);

    HttpMethod getHttpMethod();

    Map<String, String> getFormParameters();

    Locale getLocale();

    /**
     * Extracts the file suffix from the request path.
     * For example, for a path "/static/main.js", it returns ".js".
     *
     * @return The file suffix including the dot, or an empty string if not present.
     */
    default String getSuffix() {
        String path = getPath();
        if (path == null) {
            return "";
        }
        int lastDotIndex = path.lastIndexOf('.');
        if (lastDotIndex >= 0) {
            return path.substring(lastDotIndex);
        }
        return "";
    }

    String getRemoteHost();


    void addHeader(String name, String value);

    /**
     * Returns whether the request reached XIS through a secure HTTPS connection.
     * <p>
     * This is intentionally based on the externally visible scheme, not merely
     * on an internal socket detail. A production deployment may terminate TLS in
     * a proxy and forward the request to XIS over plain HTTP; in that case
     * {@code X-Forwarded-Proto: https} still means the browser sees HTTPS and
     * authentication cookies must keep their {@code Secure} attribute. For the
     * boot adapter, {@code X-Internal-Scheme} is used as the local fallback when
     * no proxy header is present.
     * <p>
     * The distinction matters for local development: Safari does not store
     * cookies marked {@code Secure} when they are received from
     * {@code http://localhost}. The login callback can therefore succeed on the
     * server while Safari silently drops the token cookies and redirects back to
     * the login page. Returning {@code false} for plain HTTP lets the response
     * writer omit only the {@code Secure} attribute in that development setup.
     * This is not a production security reduction, because HTTPS requests still
     * return {@code true} and production token cookies remain {@code Secure}.
     * <p>
     * Adapters should override this method when their native request object can
     * answer the question more directly.
     *
     * @return {@code true} if secure cookies may be emitted with the Secure flag
     */
    default boolean isSecure() {
        String forwardedProto = getHeader("X-Forwarded-Proto");
        if (forwardedProto != null && !forwardedProto.isBlank()) {
            return "https".equalsIgnoreCase(forwardedProto);
        }
        String internalScheme = getHeader("X-Internal-Scheme");
        return internalScheme != null && "https".equalsIgnoreCase(internalScheme);
    }
}
