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
     * Pending update-event messages per clientId, keyed by eventId.
     * An event is added here when it is sent and removed only when the client
     * confirms delivery via {@code push-ack}. On reconnect all still-pending
     * events are re-sent – duplicate delivery is acceptable (idempotent refresh).
     */
    private final Map<String, Map<String, WSUpdateEventMessage>> pendingRefreshEvents = new ConcurrentHashMap<>();

    public void processRequest(String message, WSEmitter emitter) {
        var requestJsonObject = gsonProvider.getGson().fromJson(message, JsonObject.class);

        var clientId = clientId(requestJsonObject);
        var requestType = requestType(requestJsonObject);

        log.debug("processRequest: clientId={} type={} emitterOpen={}",
                clientId, requestType, emitter.isOpen());

        try {
            switch (requestType) {
                case CONNECT -> processConnectMessage(clientId, emitter);
                case RECONNECT -> processReconnectMessage(clientId, emitter);
                case PING -> processPing(clientId, emitter);
                case PUSH_ACK -> processPushAck(clientId, requestJsonObject);
                case CLIENT_REQUEST -> processClientRequest(requestJsonObject, clientId, emitter);
                default -> throw new IllegalArgumentException("requestType: " + requestType);
            }
        } catch (Exception e) {
            var messageId = optionalMessageId(requestJsonObject);
            log.error("processRequest: error clientId={} messageId={}: {}", clientId, messageId, e.getMessage(), e);
            messageId.ifPresent(id -> handleException(id, e, emitter));
        }
    }

    private String clientId(JsonObject requestJsonObject) {
        return Optional.ofNullable(requestJsonObject.get("clientId"))
                .map(JsonElement::getAsString)
                .orElseThrow(WSMessageFormatException::new);
    }

    private Optional<Long> optionalMessageId(JsonObject requestJsonObject) {
        return Optional.ofNullable(requestJsonObject.get("messageId"))
                .map(JsonElement::getAsLong);
    }

    private WSRequestType requestType(JsonObject requestJsonObject) {
        return Optional.ofNullable(requestJsonObject.get("request-type"))
                .map(JsonElement::getAsString)
                .map(WSRequestType::fromValue)
                .orElseThrow(WSMessageFormatException::new);
    }

    private void processPing(String clientId, WSEmitter emitter) {
        log.debug("processPing: clientId={} – updating emitter and sending PONG", clientId);
        registerEmitter(clientId, emitter);
        emitter.send(new WSPongMessage());
    }

    private void processPushAck(String clientId, JsonObject requestJsonObject) {
        var eventId = requestJsonObject.get("eventId").getAsString();
        log.debug("processPushAck: clientId={} eventId={}", clientId, eventId);
        var pending = pendingRefreshEvents.get(clientId);
        if (pending != null) {
            pending.remove(eventId);
            log.debug("processPushAck: removed eventId={} from pending for clientId={}", eventId, clientId);
        }
    }

    public Optional<WSEmitter> getEmitter(String clientId) {
        return Optional.ofNullable(emitterMap.get(clientId));
    }

    public Collection<WSEmitter> getAllEmitters() {
        return emitterMap.values();
    }

    public void unregisterSession(String clientId, Object channel) {
        var current = emitterMap.get(clientId);
        if (current != null && !current.isChannel(channel)) {
            log.debug("unregisterSession: clientId={} – ignoring close of stale channel, current channel is different", clientId);
            return;
        }
        var pending = pendingRefreshEvents.get(clientId);
        log.debug("unregisterSession: clientId={} pendingEvents={}",
                clientId, pending != null ? pending.keySet() : "[]");
        emitterMap.remove(clientId);
        // pendingRefreshEvents intentionally kept – events are re-sent on reconnect
        // and removed only after the client sends a push-ack.
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
        var message = new WSUpdateEventMessage(updateEventKey);
        pendingRefreshEvents
                .computeIfAbsent(clientId, k -> new ConcurrentHashMap<>())
                .put(message.getEventId(), message);
        var emitter = emitterMap.get(clientId);
        if (emitter != null) {
            flushPendingRefreshEvents(clientId, emitter);
        } else {
            log.debug("sendUpdateEvent: clientId={} offline, buffering event key='{}' eventId={}",
                    clientId, updateEventKey, message.getEventId());
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

    private void registerEmitter(String clientId, WSEmitter newEmitter) {
        var old = emitterMap.put(clientId, newEmitter);
        log.debug("registerEmitter: clientId={} newEmitter=@{} oldEmitter=@{}",
                clientId,
                Integer.toHexString(System.identityHashCode(newEmitter)),
                old != null ? Integer.toHexString(System.identityHashCode(old)) : "none");
    }

    private void processConnectMessage(String clientId, WSEmitter emitter) {
        log.debug("processConnectMessage: clientId={}", clientId);
        registerEmitter(clientId, emitter);
        flushPendingRefreshEvents(clientId, emitter);
    }

    private void processReconnectMessage(String clientId, WSEmitter emitter) {
        var pending = pendingRefreshEvents.get(clientId);
        log.debug("processReconnectMessage: clientId={} pendingEvents={}",
                clientId, pending != null ? pending.keySet() : "[]");
        registerEmitter(clientId, emitter);
        flushPendingRefreshEvents(clientId, emitter);
    }

    /**
     * Re-sends all not-yet-acknowledged push messages to a client that (re)connected.
     * Events stay in the buffer until the client confirms with push-ack.
     */
    private void flushPendingRefreshEvents(String clientId, WSEmitter emitter) {
        var pending = pendingRefreshEvents.get(clientId);
        if (pending == null || pending.isEmpty()) {
            log.debug("flushPendingEvents: clientId={} emitter=@{} – nothing to flush",
                    clientId, Integer.toHexString(System.identityHashCode(emitter)));
            return;
        }
        log.debug("flushPendingEvents: clientId={} emitter=@{} re-sending {} unacknowledged event(s)",
                clientId, Integer.toHexString(System.identityHashCode(emitter)), pending.size());
        for (var message : pending.values()) {
            log.debug("flushPendingEvents: clientId={} emitter=@{} re-sending key='{}' eventId={}",
                    clientId, Integer.toHexString(System.identityHashCode(emitter)),
                    message.getUpdateEventKey(), message.getEventId());
            emitter.send(message);
        }
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
        log.debug("processClientRequest: clientId={}", clientId);
        var wsClientRequest = gsonProvider.getGson().fromJson(wsClientRequestJsonObject, WSClientRequest.class);
        if (!wsClientRequest.getClientId().equals(clientId)) {
            throw new IllegalStateException("clientId mismatch in client-request");
        }
        switch (wsClientRequest.getPath()) {
            case "/xis/page/model" -> processPageModelRequest(wsClientRequest, emitter);
            case "/xis/form/model" -> processFormModelRequest(wsClientRequest, emitter);
            case "/xis/widget/model" -> processWidgetModelRequest(wsClientRequest, emitter);
            case "/xis/page/action", "/xis/widget/action", "/xis/form/action" ->
                    processActionRequest(wsClientRequest, emitter);
            default -> throw new IllegalArgumentException("unknown path: " + wsClientRequest.getPath());
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
