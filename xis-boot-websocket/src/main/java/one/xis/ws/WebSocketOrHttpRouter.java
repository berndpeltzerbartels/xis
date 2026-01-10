package one.xis.ws;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import lombok.RequiredArgsConstructor;
import one.xis.boot.netty.NettyHttpServerHandler;

@RequiredArgsConstructor
class WebSocketOrHttpRouter extends ChannelInboundHandlerAdapter {
    private final NettyWSServerHandler wsHandler;
    private final NettyHttpServerHandler httpHandler;

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) {
        if (evt == WebSocketServerProtocolHandler.ServerHandshakeStateEvent.HANDSHAKE_COMPLETE) {
            // WebSocket erfolgreich → HTTP-Handler entfernen
            ctx.pipeline().remove(httpHandler);
            ctx.pipeline().addLast(wsHandler);
            ctx.pipeline().remove(this);
        }
        ctx.fireUserEventTriggered(evt);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof FullHttpRequest) {
            // Normaler HTTP-Request → an HTTP-Handler
            httpHandler.channelRead(ctx, msg);
        } else if (msg instanceof WebSocketFrame) {
            // WebSocket-Frame → an WS-Handler
            wsHandler.channelRead(ctx, msg);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        // Zentrale Exception-Behandlung
        // log.error("Error in channel: {}", cause.getMessage(), cause);
        // TODO
        ctx.close();
    }
}