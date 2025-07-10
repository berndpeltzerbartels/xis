package one.xis.http;

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
    String getRealPath();

    Map<String, String> getQueryParameters();

    byte[] getBody();

    default String getBodyAsString() {
        return new String(getBody());
    }

    ContentType getContentType();

    int getContentLength();

    Map<String, String> getHeaders();

    HttpMethod getHttpMethod();

    Object getBodyAsBytes();

    Map<String, String> getFormParameters();
}
