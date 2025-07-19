package one.xis.context;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import lombok.RequiredArgsConstructor;
import one.xis.http.HttpResponse;
import one.xis.http.RequestContext;
import one.xis.server.ClientRequest;
import one.xis.server.FrontendService;
import one.xis.server.ResourcePathProvider;
import one.xis.server.ServerResponse;
import one.xis.validation.ValidatorMessages;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Locale;
import java.util.Map;
import java.util.function.Supplier;

import static one.xis.context.BackendBridgeVerboseRunner.run;

@XISComponent
@SuppressWarnings("unused")
@RequiredArgsConstructor
public class BackendBridge implements ResourcePathProvider {

    private final FrontendService frontendService;
    private final Gson gson;
    private final AppContext appContext;

    public BackendBridgeResponse getComponentConfig(String uri, Map<String, String> headers) {
        return executeInRequestContext(uri, null, headers, () -> toBridgeResponse(run(frontendService::getConfig)));
    }

    public BackendBridgeResponse getPageModel(String uri, String requestJson, Map<String, String> headers) {
        return executeInRequestContext(uri, requestJson, headers, () -> toBridgeResponse(run(frontendService::processModelDataRequest, request(requestJson, headers))));
    }

    public BackendBridgeResponse getFormModel(String uri, String requestJson, Map<String, String> headers) {
        return executeInRequestContext(uri, requestJson, headers, () -> toBridgeResponse(run(frontendService::processFormDataRequest, request(requestJson, headers))));
    }

    public BackendBridgeResponse getWidgetModel(String uri, String requestJson, Map<String, String> headers) {
        return executeInRequestContext(uri, requestJson, headers, () -> toBridgeResponse(run(frontendService::processModelDataRequest, request(requestJson, headers))));
    }

    public BackendBridgeResponse onPageLinkAction(String uri, String requestJson, Map<String, String> headers) {
        return executeInRequestContext(uri, requestJson, headers, () -> toBridgeResponse(run(frontendService::processActionRequest, request(requestJson, headers))));
    }

    public BackendBridgeResponse onWidgetLinkAction(String uri, String requestJson, Map<String, String> headers) {
        return executeInRequestContext(uri, requestJson, headers, () -> toBridgeResponse(run(frontendService::processActionRequest, request(requestJson, headers))));
    }

    public BackendBridgeResponse onFormAction(String uri, String requestJson, Map<String, String> headers) {
        return executeInRequestContext(uri, requestJson, headers, () -> toBridgeResponse(run(frontendService::processActionRequest, request(requestJson, headers))));
    }

    public BackendBridgeResponse getPageHead(String uri, Map<String, String> headers) {
        return executeInRequestContext(uri, null, headers, () -> stringToBridgeResponse(frontendService.getPageHead(headers.get("uri"))));
    }

    public BackendBridgeResponse getPageBody(String uri, Map<String, String> headers) {
        return executeInRequestContext(uri, null, headers, () -> stringToBridgeResponse(frontendService.getPageBody(headers.get("uri"))));
    }

    public BackendBridgeResponse getBodyAttributes(String uri, Map<String, String> headers) {
        return executeInRequestContext(uri, null, headers, () -> toBridgeResponse(frontendService.getBodyAttributes(headers.get("uri"))));
    }

    public BackendBridgeResponse getWidgetHtml(String uri, Map<String, String> headers) {
        return executeInRequestContext(uri, null, headers, () -> stringToBridgeResponse(frontendService.getWidgetHtml(headers.get("uri"))));
    }

    private BackendBridgeResponse executeInRequestContext(String uri, String requestJson, Map<String, String> headers, Supplier<BackendBridgeResponse> action) {
        addRequestContext(uri, requestJson, headers);
        try {
            return action.get();
        } finally {
            clearRequestContext();
        }
    }

    private ClientRequest request(String requestJson, Map<String, String> headers) {
        try {
            var request = gson.fromJson(requestJson, ClientRequest.class);
            request.setLocale(Locale.GERMANY); // TODO konfigurierbar machen
            request.setZoneId("Europe/Berlin");
            var authenticationHeader = headers.get("Authorization");
            if (authenticationHeader != null && authenticationHeader.startsWith("Bearer ")) {
                request.setAccessToken(authenticationHeader.substring("Bearer ".length()));
            }
            return request;
        } catch (JsonSyntaxException e) {
            throw new RuntimeException("Failed to deserialize request", e);
        }
    }

    private <T> BackendBridgeResponse toBridgeResponse(T o) {
        return new BackendBridgeResponse(serialialize(o), 200, new ValidatorMessages());
    }

    private <T> BackendBridgeResponse toBridgeResponse(ServerResponse o) {
        BackendBridgeResponse response;
        if (o.getRedirectUrl() == null) {
            response = new BackendBridgeResponse(serialialize(o), o.getStatus(), o.getValidatorMessages());
        } else {
            response = new BackendBridgeResponse(o.getRedirectUrl(), 302, o.getValidatorMessages());
        }
        return response;
    }

    private String serialialize(Object o) {
        return gson.toJson(o);
    }

    private BackendBridgeResponse stringToBridgeResponse(String str) {
        return new BackendBridgeResponse(str, 200, new ValidatorMessages());
    }

    @Override
    public String getCustomStaticResourcePath() {
        return "public";
    }

    private void addRequestContext(String uri, String requestJson, Map<String, String> headers) {
        var request = new BackendBridgeHttpRequest(uri, requestJson, headers);
        var response = (HttpResponse) Proxy.newProxyInstance(getClass().getClassLoader(), new Class[]{HttpResponse.class}, (Object proxy, Method method, Object... args) -> null);
        RequestContext.createInstance(request, response);
    }

    private void clearRequestContext() {
        RequestContext.clear();
    }

}