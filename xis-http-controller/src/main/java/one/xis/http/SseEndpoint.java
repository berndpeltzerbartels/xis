package one.xis.http;

/**
 * Framework-specific SSE endpoint. Implementations (Netty, Spring, etc.) open
 * a streaming connection and register the resulting {@link SseEmitter} with
 * {@code SseService}. The method is called by {@code MainController} for every
 * {@code GET /xis/events} request.
 */
public interface SseEndpoint {

    /**
     * Opens an SSE connection for the given client.
     *
     * @param clientId the client id supplied via the {@code X-Client-Id} request header
     * @param userId   the optional authenticated user id associated with the client
     * @param request  the current HTTP request (may be needed to access the raw channel)
     * @param response the current HTTP response (used to set headers / keep connection open)
     */
    void open(String clientId, String userId, HttpRequest request, HttpResponse response);
}
