package one.xis.ws;

import lombok.RequiredArgsConstructor;
import one.xis.gson.GsonProvider;
import one.xis.server.FrontendService;
import one.xis.utils.lang.ClassUtils;

import java.util.Collection;

@RequiredArgsConstructor
public class WSService {

    private final FrontendService frontendService;
    private final GsonProvider gsonProvider;
    private final Collection<WSExceptionHandler<?>> exceptionHandlers;

    public void processClientRequest(String message, WSEmitter emitter) {
        WSClientRequest wsClientRequest = null;
        try {
            wsClientRequest = gsonProvider.getGson().fromJson(message, WSClientRequest.class);
            processClientRequest(wsClientRequest, emitter);
        } catch (Exception e) {
            handleException(wsClientRequest, e, emitter);
        }
    }

    @SuppressWarnings("unchecked")
    private void handleException(WSClientRequest request, Exception exception, WSEmitter emitter) {
        for (WSExceptionHandler<?> handler : exceptionHandlers) {
            if (ClassUtils.getGenericInterfacesTypeParameter(handler.getClass(), WSExceptionHandler.class, 0).isInstance(exception)) {
                var typedHandler = (WSExceptionHandler<Exception>) handler;
                var response = typedHandler.handleException(request, exception);
                response.setMessageId(request.getMessageId());
                emitter.send(response);
                return;
            }
        }

        // No handler found - send generic 500 error
        var errorResponse = new WSServerResponse(500);
        if (request != null) {
            errorResponse.setMessageId(request.getMessageId());
        }
        errorResponse.setBody(null);
        emitter.send(errorResponse);
    }

    private void processClientRequest(WSClientRequest wsClientRequest, WSEmitter emitter) {
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
