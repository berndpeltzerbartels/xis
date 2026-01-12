package one.xis.ws;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.websocketx.PingWebSocketFrame;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.extern.slf4j.Slf4j;
import one.xis.boot.netty.NettyWSPingHandler;
import one.xis.context.Component;

@Slf4j
@Component
@ChannelHandler.Sharable
class NettyWSPingHandlerImpl extends ChannelInboundHandlerAdapter implements NettyWSPingHandler {

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent e = (IdleStateEvent) evt;
            if (e.state() == IdleState.READER_IDLE) {
                // No data received for READER_IDLE_SECONDS → send ping to keep connection alive
                log.debug("Sending WebSocket ping to {}", ctx.channel().remoteAddress());
                ctx.writeAndFlush(new PingWebSocketFrame());
            }
        }
        ctx.fireUserEventTriggered(evt);
    }
}
