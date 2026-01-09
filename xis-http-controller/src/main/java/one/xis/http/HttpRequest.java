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
}
