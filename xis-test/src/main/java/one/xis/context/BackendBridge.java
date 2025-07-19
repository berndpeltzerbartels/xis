package one.xis.context;

import lombok.RequiredArgsConstructor;
import one.xis.http.HttpMethod;
import one.xis.http.RestControllerService;

import java.util.Map;

@RequiredArgsConstructor
public class BackendBridge {
    private final RestControllerService restControllerService;

    public JavascriptResponse invokeBackend(String httpMethod, String uri, Map<String, String> headers, String body) {
        var response = new HttpTestResponse();
        restControllerService.doInvocation(new HttpTestRequest(HttpMethod.fromString(httpMethod), uri, body, headers), response);
        return JavascriptResponse.builder()
                .responseText(response.getBody())
                .status(response.getStatusCode())
                .headers(response.getHeaders())
                .build();
    }
}
