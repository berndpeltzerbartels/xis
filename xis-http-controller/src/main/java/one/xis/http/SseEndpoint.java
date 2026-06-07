package one.xis.http;

import java.util.function.Consumer;

/**
 * Framework-specific SSE endpoint. Implementations (Netty, Spring, etc.) open
 * a streaming connection and expose the resulting {@link SseEmitter} to the
 * application-level code that wants to register or publish through it.
 */
public interface SseEndpoint {

    /**
     * Opens an SSE connection for the current HTTP request.
     *
     * @param request the current HTTP request (may be needed to access the raw channel)
     * @param response the current HTTP response (used to set headers / keep connection open)
     * @param onOpen called after the transport created the emitter
     * @param onClose called when the transport detects that the emitter is closed
     */
    void open(HttpRequest request, HttpResponse response, Consumer<SseEmitter> onOpen, Consumer<SseEmitter> onClose);

    /**
     * Compatibility entry point for the original XIS refresh-event flow.
     * New code should use {@link #open(HttpRequest, HttpResponse, Consumer, Consumer)}
     * and choose its own connection identity above the transport layer.
     */
    default void open(String clientId, String userId, HttpRequest request, HttpResponse response) {
        throw new UnsupportedOperationException("SSE transport opened without an application-level registry");
    }
}
