package one.xis.http.netty;

import lombok.extern.slf4j.Slf4j;
import one.xis.context.Component;
import one.xis.http.HttpRequest;
import one.xis.http.HttpResponse;
import one.xis.http.SseEmitter;
import one.xis.http.SseEndpoint;

import java.util.function.Consumer;

@Slf4j
@Component
public class NettySseEndpoint implements SseEndpoint {

    @Override
    public void open(HttpRequest request, HttpResponse response, Consumer<SseEmitter> onOpen, Consumer<SseEmitter> onClose) {
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
        onOpen.accept(emitter);
        nettyRequest.getChannel().closeFuture()
                .addListener(f -> onClose.accept(emitter));

        // Signal RestControllerServiceImpl to leave the connection open (no response body).
        response.setStatusCode(200);
        nettyResponse.markHandledExternally();
        log.debug("open: SSE connection opened");
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
