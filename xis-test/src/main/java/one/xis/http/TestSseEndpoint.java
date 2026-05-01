package one.xis.http;

import one.xis.context.Component;

@Component
public class TestSseEndpoint implements SseEndpoint {

    @Override
    public void open(String clientId, String userId, HttpRequest request, HttpResponse response) {
    }
}
