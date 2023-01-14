package one.xis.test.mocks;

import lombok.RequiredArgsConstructor;

import java.util.Map;
import java.util.function.Consumer;

@RequiredArgsConstructor
public class HttpClient {

    private final HttpMock httpMock;

    @SuppressWarnings("unused")
    public void post(String uri, Map<String, String> headers, Map<String, Object> payload, Consumer<Map<String, Object>> handler) {
        handler.accept(httpMock.handleRequest(uri, headers, payload));
    }
}
