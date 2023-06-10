package one.xis.context;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import one.xis.server.FrontendService;
import one.xis.server.Request;

import java.util.Map;

/**
 * Mediator between the compiled javascript and backendclasses.
 */
@XISComponent
@SuppressWarnings("unused")
@RequiredArgsConstructor
public class BackendBridge {

    private final FrontendService frontendService;
    private final ObjectMapper objectMapper = new ObjectMapper(); // TODO always use the same mapper and inject it here


    public String getComponentConfig(String uri, Map<String, String> headers) {
        return serialialize(frontendService.getConfig());
    }


    public String getPageModel(String uri, String requestJson, Map<String, String> headers) {
        return serialialize(frontendService.invokePageModelMethods(request(requestJson)));
    }


    public String getWidgetModel(String uri, String requestJson, Map<String, String> headers) {
        return serialialize(frontendService.invokeWidgetModelMethods(request(requestJson)));
    }


    public String onPageAction(String uri, String requestJson, Map<String, String> headers) {
        return serialialize(frontendService.invokePageActionMethod(request(requestJson)));
    }


    public String onWidgetAction(String uri, String requestJson, Map<String, String> headers) {
        return serialialize(frontendService.invokeWidgetActionMethod(request(requestJson)));
    }


    public String getPageHead(String uri, Map<String, String> headers) {
        return frontendService.getPageHead(headers.get("uri"));
    }


    public String getPageBody(String uri, Map<String, String> headers) {
        return frontendService.getPageBody(headers.get("uri"));
    }


    public String getBodyAttributes(String uri, Map<String, String> headers) {
        return serialialize(frontendService.getBodyAttributes(headers.get("uri")));
    }


    public String getWidgetHtml(String uri, Map<String, String> headers) {
        var id = uri.substring("/xis/widget/html/".length());
        return frontendService.getWidgetHtml(id);
    }

    private Request request(String requestJson) {
        try {
            return objectMapper.readValue(requestJson, Request.class);
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
}
