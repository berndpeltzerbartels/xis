package one.xis.ws;

import com.google.gson.JsonObject;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import lombok.RequiredArgsConstructor;
import one.xis.boot.netty.NettyServerHandler;
import one.xis.context.Component;
import one.xis.gson.GsonProvider;


@Component
@ChannelHandler.Sharable
@RequiredArgsConstructor
class NettyWSServerHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> implements NettyServerHandler {
    private final WSService wsService;
    private final GsonProvider gsonProvider;

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, TextWebSocketFrame frame) {
        var emitter = new NettyWSResponseEmitter(channelHandlerContext, gsonProvider.getGson());
        try {
            String requestJson = frame.text();
            var jsonRequest = (JsonObject) gsonProvider.getGson().toJsonTree(requestJson); // Validate JSON
            var topic = jsonRequest.get("topic").getAsString();
            switch (topic) {
                case "client-request" -> wsService.processClientRequest(jsonRequest, emitter);
                case "resource-request" -> wsService.processResourceRequest(jsonRequest, emitter);
                default -> throw new IllegalArgumentException("Unknown topic: " + topic);
            }
        } catch (Exception e) {
            // TODO emitter...
        }
    }
}
