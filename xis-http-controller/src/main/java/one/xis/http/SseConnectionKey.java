package one.xis.http;

/**
 * Application-level identity for one or more SSE connections.
 * <p>
 * The transport does not assign semantics to this key. XIS refresh events can
 * use {@code ("xis-client", clientId)}, while a standalone application can use
 * keys such as {@code ("game-player", playerId)} or {@code ("game-match", matchId)}.
 */
public record SseConnectionKey(String scope, String id) {

    public SseConnectionKey {
        if (scope == null || scope.isBlank()) {
            throw new IllegalArgumentException("SSE connection scope must not be blank");
        }
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("SSE connection id must not be blank");
        }
    }

    public static SseConnectionKey of(String scope, String id) {
        return new SseConnectionKey(scope, id);
    }
}
