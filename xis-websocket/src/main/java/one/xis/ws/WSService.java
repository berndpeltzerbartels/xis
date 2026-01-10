package one.xis.ws;

import com.google.gson.JsonObject;
import lombok.RequiredArgsConstructor;
import one.xis.gson.GsonProvider;
import one.xis.resource.Resource;
import one.xis.resource.StringResource;
import one.xis.server.FrontendService;

@RequiredArgsConstructor
public class WSService {

    private final FrontendService frontendService;
    private final GsonProvider gsonProvider;

    protected void processClientRequest(JsonObject message, WSEmitter emitter) {
        var wsClientRequest = gsonProvider.getGson().fromJson(message, WSClientRequest.class);
        processClientRequest(wsClientRequest, emitter);
    }

    protected void processResourceRequest(JsonObject message, WSEmitter emitter) {
        var wsResourceRequest = gsonProvider.getGson().fromJson(message, WSResourceRequest.class);
        processResourceRequest(wsResourceRequest, emitter);
    }

    private void processResourceRequest(WSResourceRequest wsResourceRequest, WSEmitter emitter) {
        switch (wsResourceRequest.getUri()) {
            case "/xis/page/config" -> {
                var clientConfig = frontendService.getConfig();
                var resource = new StringResource(gsonProvider.getGson().toJson(clientConfig), System.currentTimeMillis());
                sendResourceResponse(resource, wsResourceRequest, emitter);
            }
            case "/xis/page/head" -> {
                String pageId = wsResourceRequest.getParameters().get("pageId");
                Resource resource = frontendService.getPageHead(pageId);
                sendResourceResponse(resource, wsResourceRequest, emitter);
            }
            case "/xis/page/body" -> {
                String pageId = wsResourceRequest.getParameters().get("pageId");
                Resource resource = frontendService.getPageBody(pageId);
                sendResourceResponse(resource, wsResourceRequest, emitter);
            }
            case "/xis/page/body-attributes" -> {
                String pageId = wsResourceRequest.getParameters().get("pageId");
                Resource resource = frontendService.getBodyAttributes(pageId);
                sendResourceResponse(resource, wsResourceRequest, emitter);
            }
            case "/xis/widget/html" -> {
                String widgetId = wsResourceRequest.getParameters().get("widgetId");
                Resource resource = frontendService.getWidgetHtml(widgetId);
                sendResourceResponse(resource, wsResourceRequest, emitter);
            }
            case "/xis/include/html" -> {
                String key = wsResourceRequest.getParameters().get("key");
                Resource resource = frontendService.getIncludeHtml(key);
                sendResourceResponse(resource, wsResourceRequest, emitter);
            }
            case "/bundle.min.js" -> {
                Resource resource = frontendService.getBundleJs();
                sendResourceResponse(resource, wsResourceRequest, emitter);
            }
            case "/bundle.min.js.map" -> {
                Resource resource = frontendService.getBundleJsMap();
                sendResourceResponse(resource, wsResourceRequest, emitter);
            }
            default -> throw new IllegalArgumentException("Unknown resource URI: " + wsResourceRequest.getUri());
        }
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

    private void sendResourceResponse(Resource resource, WSResourceRequest wsResourceRequest, WSEmitter responder) {
        var response = new WSResourceResponse();
        if (wsResourceRequest.getHeaders().getLastModifiedAsEpochMilli() > 0
                && resource.getLastModified() > 0
                && resource.getLastModified() <= wsResourceRequest.getHeaders().getLastModifiedAsEpochMilli()) {
            response.setStatusCode(304);

        } else {
            response.setStatusCode(200);
            response.setServerResponse(resource.getContent());
        }
        response.getHeaders().setLastModified(resource.getLastModified());
        responder.send(response);
    }
}
