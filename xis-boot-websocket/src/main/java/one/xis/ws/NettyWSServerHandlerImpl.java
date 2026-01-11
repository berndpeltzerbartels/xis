package one.xis.ws;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import lombok.RequiredArgsConstructor;
import one.xis.boot.netty.NettyWSServerHandler;
import one.xis.context.Component;
import one.xis.gson.GsonProvider;


@Component
@ChannelHandler.Sharable
@RequiredArgsConstructor
class NettyWSServerHandlerImpl extends SimpleChannelInboundHandler<TextWebSocketFrame> implements NettyWSServerHandler {
    private final WSService wsService;
    private final GsonProvider gsonProvider;

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, TextWebSocketFrame frame) {
        var emitter = new NettyWSResponseEmitter(channelHandlerContext, gsonProvider.getGson());
        try {
            wsService.processClientRequest(frame.text(), emitter);
        } catch (Exception e) {
            // TODO emitter...
        }
    }
}
