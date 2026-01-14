package one.xis.ws;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.util.AttributeKey;
import lombok.RequiredArgsConstructor;
import one.xis.boot.netty.NettyWSServerHandler;
import one.xis.context.Component;
import one.xis.gson.GsonProvider;


@Component
@ChannelHandler.Sharable
@RequiredArgsConstructor
class NettyWSServerHandlerImpl extends SimpleChannelInboundHandler<TextWebSocketFrame> implements NettyWSServerHandler {
    private static final AttributeKey<String> CLIENT_ID_ATTR = AttributeKey.valueOf("clientId");
    
    private final WSService wsService;
    private final GsonProvider gsonProvider;

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, TextWebSocketFrame frame) {
        var emitter = new NettyWSResponseEmitter(channelHandlerContext, gsonProvider.getGson());
        try {
            // Extract and store clientId in channel attributes for cleanup
            var jsonObject = gsonProvider.getGson().fromJson(frame.text(), com.google.gson.JsonObject.class);
            var clientIdElement = jsonObject.get("clientId");
            if (clientIdElement != null && channelHandlerContext.channel().attr(CLIENT_ID_ATTR).get() == null) {
                channelHandlerContext.channel().attr(CLIENT_ID_ATTR).set(clientIdElement.getAsString());
            }
            
            wsService.processRequest(frame.text(), emitter);
        } catch (Exception e) {
            System.err.println("Error processing WebSocket request: " + e.getMessage());
            e.printStackTrace();
            sendErrorResponse(emitter, frame.text(), e);
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        // Clean up session when channel closes
        String clientId = ctx.channel().attr(CLIENT_ID_ATTR).get();
        if (clientId != null) {
            wsService.unregisterSession(clientId);
            System.out.println("WebSocket channel closed for clientId: " + clientId);
        }
        super.channelInactive(ctx);
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
