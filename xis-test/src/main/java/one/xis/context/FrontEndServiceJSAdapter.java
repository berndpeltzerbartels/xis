package one.xis.context;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import one.xis.server.Config;
import one.xis.server.FrontendService;
import one.xis.server.Request;
import one.xis.server.Response;

import java.util.HashMap;
import java.util.Map;

@XISComponent
@RequiredArgsConstructor
@SuppressWarnings({"unused", "unchecked"})
public class FrontEndServiceJSAdapter {
    private final FrontendService frontendService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public Config getConfig() {
        return frontendService.getConfig();
    }

    public Response invokePageActionMethod(Map<String, Object> map) {
        return frontendService.invokePageActionMethod(createRequest(map));
    }

    public Response invokeWidgetActionMethod(Map<String, Object> map) {
        return frontendService.invokeWidgetActionMethod(createRequest(map));
    }

    public Response invokePageModelMethods(Map<String, Object> map) {
        return frontendService.invokePageModelMethods(createRequest(map));
    }

    public Response invokeWidgetModelMethods(Map<String, Object> map) {
        return frontendService.invokeWidgetModelMethods(createRequest(map));
    }

    public String getPageHead(String id) {
        return frontendService.getPageHead(id);
    }

    public String getPageBody(String id) {
        return frontendService.getPageBody(id);
    }

    public Map<String, String> getBodyAttributes(String id) {
        return frontendService.getBodyAttributes(id);
    }

    public String getWidgetHtml(String id) {
        return frontendService.getWidgetHtml(id);
    }

    public String getRootPageHtml() {
        return frontendService.getRootPageHtml();
    }

    private Request createRequest(Map<String, Object> map) {
        var request = new Request();
        request.setControllerId((String) map.get("controllerId"));
        request.setUserId((String) map.get("userId"));
        request.setClientId((String) map.get("clientId"));
        request.setAction((String) map.get("action"));
        var srcMap = (Map<String, Object>) map.get("data");
        var resultMap = new HashMap<String, String>();
        srcMap.forEach((name, value) -> {
            try {
                resultMap.put(name, objectMapper.writeValueAsString(value));
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        });
        request.setData(resultMap);
        return request;
    }

}
