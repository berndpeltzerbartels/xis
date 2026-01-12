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
            System.err.println("Error processing WebSocket request: " + e.getMessage());
            e.printStackTrace();
            sendErrorResponse(emitter, frame.text(), e);
        }
    }

    private void sendErrorResponse(NettyWSResponseEmitter emitter, String requestJson, Exception e) {
        try {
            var request = gsonProvider.getGson().fromJson(requestJson, WSClientRequest.class);
            var errorResponse = new WSServerResponse(500);
            errorResponse.setMessageId(request.getMessageId());
            errorResponse.setBody(null);
            emitter.send(errorResponse);
        } catch (Exception parseError) {
            System.err.println("Failed to parse request for error response: " + parseError.getMessage());
        }
    }
}
