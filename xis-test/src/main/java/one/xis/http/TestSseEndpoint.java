package one.xis.http;

import one.xis.context.Component;

import java.util.function.Consumer;

@Component
public class TestSseEndpoint implements SseEndpoint {

    @Override
    public void open(HttpRequest request, HttpResponse response, Consumer<SseEmitter> onOpen, Consumer<SseEmitter> onClose) {
    }
}
