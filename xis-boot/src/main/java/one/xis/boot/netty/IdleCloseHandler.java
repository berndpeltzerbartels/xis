package one.xis.boot.netty;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleStateEvent;

/**
 * Closes channels that stay idle for too long.
 * This prevents slow leaks caused by many open keep-alive connections.
 */
@ChannelHandler.Sharable
public final class IdleCloseHandler extends ChannelInboundHandlerAdapter {

    public static final IdleCloseHandler INSTANCE = new IdleCloseHandler();

    private IdleCloseHandler() {
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) {
        if (evt instanceof IdleStateEvent) {
            ctx.close();
            return;
        }
        ctx.fireUserEventTriggered(evt);
    }
}
