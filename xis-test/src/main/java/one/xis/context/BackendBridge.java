package one.xis.context;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import one.xis.server.ClientRequest;
import one.xis.server.FrontendService;
import one.xis.server.ServerResponse;
import one.xis.server.StaticResourcePathProvider;
import one.xis.validation.ValidatorMessages;

import java.util.Locale;
import java.util.Map;

import static one.xis.context.BackendBridgeVerboseRunner.run;

/**
 * Mediator between the compiled javascript and backend classes during testing.
 */
@XISComponent
@SuppressWarnings("unused")
@RequiredArgsConstructor
public class BackendBridge implements StaticResourcePathProvider {

    private final FrontendService frontendService;
    private final ObjectMapper objectMapper = new ObjectMapper(); // TODO always use the same mapper and inject it here
    private final AppContext appContext;

    public BackendBridgeResponse getComponentConfig(String uri, Map<String, String> headers) {
        return toBridgeResponse(run(frontendService::getConfig));
    }

    public BackendBridgeResponse getPageModel(String uri, String requestJson, Map<String, String> headers) {
        return toBridgeResponse(run(frontendService::processModelDataRequest, request(requestJson)));
    }

    public BackendBridgeResponse getFormModel(String uri, String requestJson, Map<String, String> headers) {
        return toBridgeResponse(run(frontendService::processFormDataRequest, request(requestJson)));
    }

    public BackendBridgeResponse getWidgetModel(String uri, String requestJson, Map<String, String> headers) {
        return toBridgeResponse(run(frontendService::processModelDataRequest, request(requestJson)));
    }

    public BackendBridgeResponse onPageLinkAction(String uri, String requestJson, Map<String, String> headers) {
        return toBridgeResponse(run(frontendService::processActionRequest, request(requestJson)));
    }

    public BackendBridgeResponse onWidgetLinkAction(String uri, String requestJson, Map<String, String> headers) {
        return toBridgeResponse(run(frontendService::processActionRequest, request(requestJson)));
    }

    public BackendBridgeResponse onFormAction(String uri, String requestJson, Map<String, String> headers) {
        return toBridgeResponse(run(frontendService::processActionRequest, request(requestJson)));
    }

    public BackendBridgeResponse getPageHead(String uri, Map<String, String> headers) {
        return stringToBridgeResponse(run(frontendService::getPageHead, headers.get("uri")));
    }

    public BackendBridgeResponse getPageBody(String uri, Map<String, String> headers) {
        return stringToBridgeResponse(run(frontendService::getPageBody, headers.get("uri")));
    }

    public BackendBridgeResponse getBodyAttributes(String uri, Map<String, String> headers) {
        return toBridgeResponse(run(frontendService::getBodyAttributes, headers.get("uri")));
    }

    public BackendBridgeResponse getWidgetHtml(String uri, Map<String, String> headers) {
        return stringToBridgeResponse(run(frontendService::getWidgetHtml, headers.get("uri")));
    }

    private BackendBridgeResponse stringToBridgeResponse(String str) {
        return new BackendBridgeResponse(str, 200, new ValidatorMessages());
    }


    private <T> BackendBridgeResponse toBridgeResponse(T o) {
        return new BackendBridgeResponse(serialialize(o), 200, new ValidatorMessages());
    }


    private <T> BackendBridgeResponse toBridgeResponse(ServerResponse o) {
        return new BackendBridgeResponse(serialialize(o), o.getStatus(), o.getValidatorMessages());
    }


    private String resourceId(String uri) {
        return uri.substring(uri.lastIndexOf("/") + 1);
    }

    private ClientRequest request(String requestJson) {
        try {
            var request = objectMapper.readValue(requestJson, ClientRequest.class);
            request.setLocale(Locale.GERMANY); // TODO
            request.setZoneId("Europe/Berlin");
            return request;
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to deserialize request", e);
        }
    }

    private String serialialize(Object o) {
        try {
            return objectMapper.writeValueAsString(o);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize", e);
        }
    }

    @Override
    public String getCustomStaticResourcePath() {
        return "public";
    }
}
