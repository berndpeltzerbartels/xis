package one.xis.ws;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import one.xis.UserContextImpl;
import one.xis.context.Component;
import one.xis.gson.GsonProvider;
import one.xis.server.FrontendService;
import one.xis.utils.lang.ClassUtils;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

@Slf4j
@Component
@RequiredArgsConstructor
public class WSService {

    private final FrontendService frontendService;
    private final GsonProvider gsonProvider;
    private final Collection<WSExceptionHandler<?>> exceptionHandlers;
    private final Map<String, WSEmitter> emitterMap = new ConcurrentHashMap<>();

    /**
     * Per-clientId lock objects. Used to synchronize sendUpdateEvent and
     * precessReconnectMessage so that buffering and flushing never race.
     */
    private final Map<String, Object> clientLocks = new ConcurrentHashMap<>();

    /**
     * Pending update-event keys per clientId.
     * Events are buffered here when the client is temporarily disconnected
     * and flushed on reconnect. Using a Set automatically deduplicates
     * multiple events with the same key that arrive while the client is offline.
     */
    private final Map<String, Set<String>> pendingEvents = new ConcurrentHashMap<>();

    public void processRequest(String message, WSEmitter emitter) {
        var requestJsonObject = gsonProvider.getGson().fromJson(message, JsonObject.class);

        var clientId = Optional.ofNullable(requestJsonObject.get("clientId"))
                .map(JsonElement::getAsString)
                .orElseThrow(WSMessageFormatException::new);

        var messageId = Optional.ofNullable(requestJsonObject.get("messageId"))
                .map(JsonElement::getAsLong)
                .orElseThrow(WSMessageFormatException::new);

        var requestType = WSRequestType.fromValue(requestJsonObject.get("request-type").getAsString());
        log.debug("processRequest: clientId={} messageId={} type={} emitterOpen={}",
                clientId, messageId, requestType, emitter.isOpen());

        // Register/update emitter for this clientId
        emitterMap.put(clientId, emitter);

        try {
            switch (requestType) {
                case RECONNECT -> precessReconnectMessage(clientId, emitter);
                case CLIENT_REQUEST -> processClientRequest(requestJsonObject, clientId, emitter);
                default -> throw new IllegalArgumentException("requestType: " + requestType);
            }
        } catch (Exception e) {
            log.error("processRequest: error clientId={} messageId={}: {}", clientId, messageId, e.getMessage(), e);
            handleException(messageId, e, emitter);
        }
    }

    public Optional<WSEmitter> getEmitter(String clientId) {
        return Optional.ofNullable(emitterMap.get(clientId));
    }

    public Collection<WSEmitter> getAllEmitters() {
        return emitterMap.values();
    }

    public void unregisterSession(String clientId) {
        log.debug("unregisterSession: clientId={} pendingEvents={}", clientId, pendingEvents.get(clientId));
        var lock = clientLocks.computeIfAbsent(clientId, k -> new Object());
        synchronized (lock) {
            emitterMap.remove(clientId);
            pendingEvents.remove(clientId);
        }
        clientLocks.remove(clientId);
    }

    public void removeClosedSessions(Predicate<WSEmitter> isClosed) {
        log.debug("removing closed sessions");
        emitterMap.entrySet().removeIf(entry -> isClosed.test(entry.getValue()));
    }

    /**
     * Sends an update-event push message to a specific client.
     * The client will reload all pages/widgets annotated with
     * {@code @RefreshOnUpdateEvents} for the given key.
     *
     * @param clientId       the target client
     * @param updateEventKey the event key to fire
     */
    public void sendUpdateEvent(String clientId, String updateEventKey) {
        var lock = clientLocks.computeIfAbsent(clientId, k -> new Object());
        synchronized (lock) {
            var emitter = emitterMap.get(clientId);
            log.debug("sendUpdateEvent: clientId={} key='{}' emitter={} open={}",
                    clientId, updateEventKey,
                    emitter != null ? "present" : "null",
                    emitter != null ? emitter.isOpen() : false);
            if (emitter == null || !emitter.isOpen()) {
                log.debug("sendUpdateEvent: client {} offline – buffering event '{}'", clientId, updateEventKey);
                pendingEvents.computeIfAbsent(clientId, k -> ConcurrentHashMap.newKeySet()).add(updateEventKey);
                return;
            }
            emitter.send(new WSUpdateEventMessage(updateEventKey));
            if (!emitter.isOpen()) {
                log.warn("sendUpdateEvent: client {} channel closed DURING send – buffering event '{}'", clientId, updateEventKey);
                pendingEvents.computeIfAbsent(clientId, k -> ConcurrentHashMap.newKeySet()).add(updateEventKey);
            } else {
                log.debug("sendUpdateEvent: event '{}' delivered to client {}", updateEventKey, clientId);
            }
        }
    }

    /**
     * Broadcasts an update-event push message to ALL connected clients.
     * Clients that are currently offline receive the event buffered and
     * get it delivered on their next reconnect.
     *
     * @param refreshEvent the event containing the updateEventKey and the list of clientIds to send the event to
     */
    public void broadcastUpdateEvent(RefreshEvent refreshEvent) {
        refreshEvent.getClientIds().parallelStream()
                .forEach(clientId -> sendUpdateEvent(clientId, refreshEvent.getEventKey()));
    }

    public void broadcastToAllClients(String updateEventKey) {
        log.debug("broadcastToAllClients: key='{}' connectedClients={}", updateEventKey, emitterMap.size());
        emitterMap.keySet().parallelStream()
                .forEach(clientId -> sendUpdateEvent(clientId, updateEventKey));
    }

    public void broadcastUpdateEvent(WSEmitter emitter, String updateEventKey) {
        if (emitter.isOpen()) {
            emitter.send(new WSUpdateEventMessage(updateEventKey));
        }
    }

    private void precessReconnectMessage(String clientId, WSEmitter emitter) {
        var pending = pendingEvents.get(clientId);
        log.debug("precessReconnectMessage: clientId={} pendingEvents={}",
                clientId, pending != null ? pending : "[]");
        var lock = clientLocks.computeIfAbsent(clientId, k -> new Object());
        synchronized (lock) {
            emitterMap.put(clientId, emitter);
            flushPendingEvents(clientId, emitter);
        }
    }

    /**
     * Sends all buffered update-events for a client that reconnected,
     * then clears the buffer.
     */
    private void flushPendingEvents(String clientId, WSEmitter emitter) {
        var pending = pendingEvents.remove(clientId);
        if (pending == null || pending.isEmpty()) {
            log.debug("flushPendingEvents: clientId={} – nothing to flush", clientId);
            return;
        }
        log.debug("flushPendingEvents: clientId={} flushing {} event(s): {}", clientId, pending.size(), pending);
        for (var key : pending) {
            log.debug("flushPendingEvents: clientId={} sending buffered event '{}'", clientId, key);
            emitter.send(new WSUpdateEventMessage(key));
            if (!emitter.isOpen()) {
                log.warn("flushPendingEvents: clientId={} channel closed during flush – re-buffering all events: {}", clientId, pending);
                pending.forEach(k ->
                        pendingEvents.computeIfAbsent(clientId, id -> ConcurrentHashMap.newKeySet()).add(k));
                return;
            }
        }
        log.debug("flushPendingEvents: clientId={} all events flushed successfully", clientId);
    }

    @SuppressWarnings("unchecked")
    private void handleException(Long messageId, Exception exception, WSEmitter emitter) {
        for (WSExceptionHandler<?> handler : exceptionHandlers) {
            if (ClassUtils.getGenericInterfacesTypeParameter(handler.getClass(), WSExceptionHandler.class, 0).isInstance(exception)) {
                var typedHandler = (WSExceptionHandler<Exception>) handler;
                var response = typedHandler.handleException(exception);
                response.setMessageId(messageId);
                emitter.send(response);
                return;
            }
        }

        // No handler found - send generic 500 error
        var errorResponse = new WSServerResponse(500);
        errorResponse.setMessageId(messageId);
        errorResponse.setBody(null);
        emitter.send(errorResponse);
    }

    private void processClientRequest(JsonObject wsClientRequestJsonObject, String clientId, WSEmitter emitter) {
        log.debug("processing ClientRequest: {}", clientId);
        var wsClientRequest = gsonProvider.getGson().fromJson(wsClientRequestJsonObject, WSClientRequest.class);
        if (!wsClientRequest.getClientId().equals(clientId)) {
            throw new IllegalStateException("client-id values  are not equal");
        }
        switch (wsClientRequest.getPath()) {
            case "/xis/page/model" -> processPageModelRequest(wsClientRequest, emitter);
            case "/xis/form/model" -> processFormModelRequest(wsClientRequest, emitter);
            case "/xis/widget/model" -> processWidgetModelRequest(wsClientRequest, emitter);
            case "/xis/page/action", "/xis/widget/action", "/xis/form/action" ->
                    processActionRequest(wsClientRequest, emitter);
            default -> throw new IllegalArgumentException("Unknown URI: " + wsClientRequest.getPath());
        }
    }

    /**
     * After calling frontendService the TokenStatus may have been renewed (new tokens issued).
     * We write the new tokens into custom response headers so the JS client can update its cookies.
     * If nothing was renewed the headers stay empty and the client keeps its existing cookies.
     * No-op when security is not configured (TokenStatus is null or SecurityAttributes absent).
     */
    private void applyRenewedTokensToResponse(WSServerResponse wsResponse) {
        try {
            var tokenStatus = UserContextImpl.getInstance().getTokenStatus();
            if (tokenStatus != null && tokenStatus.isRenewed()) {
                wsResponse.getHeaders().put("X-Access-Token", tokenStatus.getAccessToken());
                wsResponse.getHeaders().put("X-Renew-Token", tokenStatus.getRenewToken());
                if (tokenStatus.getExpiresIn() != null) {
                    wsResponse.getHeaders().put("X-Token-Expires-In",
                            String.valueOf(tokenStatus.getExpiresIn().getSeconds()));
                }
                if (tokenStatus.getRenewExpiresIn() != null) {
                    wsResponse.getHeaders().put("X-Renew-Token-Expires-In",
                            String.valueOf(tokenStatus.getRenewExpiresIn().getSeconds()));
                }
                log.debug("applyRenewedTokensToResponse: renewed tokens written to WS response headers");
            }
        } catch (Exception e) {
            // UserContext not available (e.g. no security configured) – safe to ignore
            log.debug("applyRenewedTokensToResponse: skipped – {}", e.getMessage());
        }
    }

    private void processPageModelRequest(WSClientRequest wsClientRequest, WSEmitter emitter) {
        var response = frontendService.processModelDataRequest(wsClientRequest.getBody());
        var wsResponse = new WSServerResponse();
        wsResponse.setMessageId(wsClientRequest.getMessageId());
        wsResponse.setStatus(200);
        wsResponse.setBody(response);
        applyRenewedTokensToResponse(wsResponse);
        emitter.send(wsResponse);
    }

    private void processFormModelRequest(WSClientRequest wsClientRequest, WSEmitter responder) {
        var response = frontendService.processFormDataRequest(wsClientRequest.getBody());
        var wsResponse = new WSServerResponse();
        wsResponse.setMessageId(wsClientRequest.getMessageId());
        wsResponse.setStatus(200);
        wsResponse.setBody(response);
        applyRenewedTokensToResponse(wsResponse);
        responder.send(wsResponse);
    }

    private void processWidgetModelRequest(WSClientRequest wsClientRequest, WSEmitter responder) {
        var response = frontendService.processModelDataRequest(wsClientRequest.getBody());
        var wsResponse = new WSServerResponse();
        wsResponse.setMessageId(wsClientRequest.getMessageId());
        wsResponse.setStatus(200);
        wsResponse.setBody(response);
        applyRenewedTokensToResponse(wsResponse);
        responder.send(wsResponse);
    }

    private void processActionRequest(WSClientRequest wsClientRequest, WSEmitter responder) {
        var response = frontendService.processActionRequest(wsClientRequest.getBody());
        var wsResponse = new WSServerResponse();
        wsResponse.setMessageId(wsClientRequest.getMessageId());
        wsResponse.setStatus(200);
        wsResponse.setBody(response);
        applyRenewedTokensToResponse(wsResponse);
        responder.send(wsResponse);
    }
}
