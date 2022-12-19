package one.xis.context.mocks;

import lombok.RequiredArgsConstructor;
import one.xis.ajax.AjaxRequest;
import one.xis.ajax.AjaxResponse;
import one.xis.ajax.AjaxService;
import one.xis.ajax.ComponentType;
import one.xis.context.XISComponent;

import java.util.Date;
import java.util.Map;

@XISComponent
@RequiredArgsConstructor
public class HttpMock {

    private final AjaxService ajaxService;

    final Map<String, Object> handleRequest(@SuppressWarnings("unused") String uri, Map<String, String> headers, Map<String, Object> payload) {
        var response = ajaxService.handleRequest(ajaxRequest(headers, payload), clientId(headers), authorization(headers));
        return responseMap(response);
    }

    private String clientId(Map<String, String> headers) {
        return headers.get(AjaxService.CLIENT_ID_HEADER_NAME);
    }

    private String authorization(Map<String, String> headers) {
        return headers.get("Authorization");
    }

    private AjaxRequest ajaxRequest(Map<String, String> headers, Map<String, Object> payload) {
        var request = new AjaxRequest();
        request.setComponentType(ComponentType.valueOf((String) payload.get("componentType")));
        request.setMessages(null); // TODO: only one message a time
        request.setUrlParameters(null); // TODO may be we don't need it
        request.setTimestamp(new Date());
        return request;
    }

    private Map<String, Object> responseMap(AjaxResponse response) {
        return null;
    }
}
