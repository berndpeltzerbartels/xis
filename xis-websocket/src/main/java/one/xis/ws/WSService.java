package one.xis.ws;

import com.google.gson.JsonObject;
import lombok.RequiredArgsConstructor;
import one.xis.gson.GsonProvider;
import one.xis.server.FrontendService;

@RequiredArgsConstructor
public class WSService {

    private final FrontendService frontendService;
    private final GsonProvider gsonProvider;

    protected void processClientRequest(JsonObject message, WSEmitter emitter) {
        var wsClientRequest = gsonProvider.getGson().fromJson(message, WSClientRequest.class);
        processClientRequest(wsClientRequest, emitter);
    }

    private void processClientRequest(WSClientRequest wsClientRequest, WSEmitter emitter) {
        switch (wsClientRequest.getUri()) {
            case "/xis/page/model" -> processPageModelRequest(wsClientRequest, emitter);
            case "/xis/form/model" -> processFormModelRequest(wsClientRequest, emitter);
            case "/xis/widget/model" -> processWidgetModelRequest(wsClientRequest, emitter);
            case "/xis/page/action", "/xis/widget/action", "/xis/form/action" ->
                    processActionRequest(wsClientRequest, emitter);
            default -> throw new IllegalArgumentException("Unknown URI: " + wsClientRequest.getUri());
        }
    }

    private void processPageModelRequest(WSClientRequest wsClientRequest, WSEmitter emitter) {
        var response = frontendService.processModelDataRequest(wsClientRequest.getClientRequest());
        var wsResponse = new WSServerResponse();
        wsResponse.setStatusCode(200);
        wsResponse.setServerResponse(response);
        emitter.send(wsResponse);
    }

    private void processFormModelRequest(WSClientRequest wsClientRequest, WSEmitter responder) {
        var response = frontendService.processFormDataRequest(wsClientRequest.getClientRequest());
        var wsResponse = new WSServerResponse();
        wsResponse.setStatusCode(200);
        wsResponse.setServerResponse(response);
        responder.send(wsResponse);
    }

    private void processWidgetModelRequest(WSClientRequest wsClientRequest, WSEmitter responder) {
        var response = frontendService.processModelDataRequest(wsClientRequest.getClientRequest());
        var wsResponse = new WSServerResponse();
        wsResponse.setStatusCode(200);
        wsResponse.setServerResponse(response);
        responder.send(wsResponse);
    }

    private void processActionRequest(WSClientRequest wsClientRequest, WSEmitter responder) {
        var response = frontendService.processActionRequest(wsClientRequest.getClientRequest());
        var wsResponse = new WSServerResponse();
        wsResponse.setStatusCode(200);
        wsResponse.setServerResponse(response);
        responder.send(wsResponse);
    }
}
