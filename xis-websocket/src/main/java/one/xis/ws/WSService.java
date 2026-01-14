package one.xis.ws;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    public void processRequest(String message, WSEmitter emitter) {
        var requestJsonObject = gsonProvider.getGson().fromJson(message, JsonObject.class);

        var clientId = Optional.ofNullable(requestJsonObject.get("clientId"))
                .map(JsonElement::getAsString)
                .orElseThrow(WSMessageFormatException::new);

        var messageId = Optional.ofNullable(requestJsonObject.get("messageId"))
                .map(JsonElement::getAsLong)
                .orElseThrow(WSMessageFormatException::new);
        log.debug("processing request. clientId: {}, messageId: {}", clientId, messageId);
        // Register/update emitter for this clientId
        emitterMap.put(clientId, emitter);

        var requestType = WSRequestType.fromValue(requestJsonObject.get("request-type").getAsString());
        try {
            switch (requestType) {
                case RECONNECT -> precessReconnectMessage(clientId, emitter);
                case CLIENT_REQUEST -> processClientRequest(requestJsonObject, clientId, emitter);
                default -> throw new IllegalArgumentException("requestType: " + requestType);
            }
        } catch (Exception e) {
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
        emitterMap.remove(clientId);
    }

    public void removeClosedSessions(Predicate<WSEmitter> isClosed) {
        log.debug("removing closed sessions");
        emitterMap.entrySet().removeIf(entry -> isClosed.test(entry.getValue()));
    }

    private void precessReconnectMessage(String clientId, WSEmitter emitter) {
        log.debug("processing reconnect for clientId: {}", clientId);
        emitterMap.put(clientId, emitter);
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

    private void processPageModelRequest(WSClientRequest wsClientRequest, WSEmitter emitter) {
        var response = frontendService.processModelDataRequest(wsClientRequest.getBody());
        var wsResponse = new WSServerResponse();
        wsResponse.setMessageId(wsClientRequest.getMessageId());
        wsResponse.setStatus(200);
        wsResponse.setBody(response);
        emitter.send(wsResponse);
    }

    private void processFormModelRequest(WSClientRequest wsClientRequest, WSEmitter responder) {
        var response = frontendService.processFormDataRequest(wsClientRequest.getBody());
        var wsResponse = new WSServerResponse();
        wsResponse.setMessageId(wsClientRequest.getMessageId());
        wsResponse.setStatus(200);
        wsResponse.setBody(response);
        responder.send(wsResponse);
    }

    private void processWidgetModelRequest(WSClientRequest wsClientRequest, WSEmitter responder) {
        var response = frontendService.processModelDataRequest(wsClientRequest.getBody());
        var wsResponse = new WSServerResponse();
        wsResponse.setMessageId(wsClientRequest.getMessageId());
        wsResponse.setStatus(200);
        wsResponse.setBody(response);
        responder.send(wsResponse);
    }

    private void processActionRequest(WSClientRequest wsClientRequest, WSEmitter responder) {
        var response = frontendService.processActionRequest(wsClientRequest.getBody());
        var wsResponse = new WSServerResponse();
        wsResponse.setMessageId(wsClientRequest.getMessageId());
        wsResponse.setStatus(200);
        wsResponse.setBody(response);
        responder.send(wsResponse);
    }
}
