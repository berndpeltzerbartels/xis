package one.xis.boot.netty;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import one.xis.context.Component;
import one.xis.http.HttpRequest;
import one.xis.http.HttpResponse;
import one.xis.http.SseEndpoint;
import one.xis.server.SseService;

@Slf4j
@Component
@RequiredArgsConstructor
public class NettySseEndpoint implements SseEndpoint {

    private final SseService sseService;

    @Override
    public void open(String clientId, String userId, HttpRequest request, HttpResponse response) {
        // The raw Netty channel is accessible via NettyHttpRequest.
        if (!(request instanceof NettyHttpRequest nettyRequest)) {
            throw new IllegalStateException("NettySseEndpoint requires a NettyHttpRequest, got: "
                    + request.getClass().getName());
        }
        NettySseEmitter emitter = new NettySseEmitter(nettyRequest.getChannel());
        sseService.registerEmitter(clientId, userId, emitter);
        nettyRequest.getChannel().closeFuture()
                .addListener(f -> sseService.unregisterEmitter(clientId, emitter));

        // Signal RestControllerServiceImpl to leave the connection open (no response body).
        response.setStatusCode(200);
        log.debug("open: SSE connection opened for clientId={}", clientId);
    }
}
