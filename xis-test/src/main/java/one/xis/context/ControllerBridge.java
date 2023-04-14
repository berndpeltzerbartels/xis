package one.xis.context;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import one.xis.server.Config;
import one.xis.server.FrontendService;
import one.xis.server.Request;
import one.xis.server.Response;

import java.util.Map;

@XISComponent
@SuppressWarnings("unused")
@RequiredArgsConstructor
public class ControllerBridge {

    private final FrontendService frontendService;
    private final ObjectMapper objectMapper = new ObjectMapper(); // TODO always use the same mapper and inject it here


    public Config getComponentConfig(String uri, Map<String, String> headers) {
        return frontendService.getConfig();
    }


    public Response getPageModel(String uri, String requestJson, Map<String, String> headers) {
        return frontendService.invokePageModelMethods(request(requestJson));
    }


    public Response getWidgetModel(String uri, String requestJson, Map<String, String> headers) {
        return frontendService.invokeWidgetModelMethods(request(requestJson));
    }


    public Response onPageAction(String uri, String requestJson, Map<String, String> headers) {
        return frontendService.invokePageActionMethod(request(requestJson));
    }


    public Response onWidgetAction(String uri, String requestJson, Map<String, String> headers) {
        return frontendService.invokeWidgetActionMethod(request(requestJson));
    }


    public String getPageHead(String uri, Map<String, String> headers) {
        return frontendService.getPageHead(headers.get("uri"));
    }


    public String getPageBody(String uri, Map<String, String> headers) {
        return frontendService.getPageBody(headers.get("uri"));
    }


    public Map<String, String> getBodyAttributes(String uri, Map<String, String> headers) {
        return frontendService.getBodyAttributes(headers.get("uri"));
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
}
