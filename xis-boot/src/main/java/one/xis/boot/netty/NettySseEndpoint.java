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
        if (!(response instanceof NettyHttpResponse nettyResponse)) {
            throw new IllegalStateException("NettySseEndpoint requires a NettyHttpResponse, got: "
                    + response.getClass().getName());
        }
        removeIdleHandlers(nettyRequest);
        NettySseEmitter emitter = new NettySseEmitter(nettyRequest.getChannel(), request.getHeader("Origin"));
        sseService.registerEmitter(clientId, userId, emitter);
        emitter.send(": connected\n\n").whenComplete((ignored, throwable) -> {
            if (throwable != null) {
                sseService.unregisterEmitter(clientId, emitter);
            }
        });
        nettyRequest.getChannel().closeFuture()
                .addListener(f -> sseService.unregisterEmitter(clientId, emitter));

        // Signal RestControllerServiceImpl to leave the connection open (no response body).
        response.setStatusCode(200);
        nettyResponse.markHandledExternally();
        log.debug("open: SSE connection opened for clientId={}", clientId);
    }

    private void removeIdleHandlers(NettyHttpRequest request) {
        var pipeline = request.getChannel().pipeline();
        if (pipeline.get(IdleCloseHandler.class) != null) {
            pipeline.remove(IdleCloseHandler.class);
        }
        if (pipeline.get(io.netty.handler.timeout.IdleStateHandler.class) != null) {
            pipeline.remove(io.netty.handler.timeout.IdleStateHandler.class);
        }
    }
}
