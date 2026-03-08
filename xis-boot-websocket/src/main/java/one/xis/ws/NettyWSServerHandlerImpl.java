package one.xis.ws;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.util.AttributeKey;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import one.xis.boot.netty.NettyWSServerHandler;
import one.xis.context.Component;
import one.xis.gson.GsonProvider;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@ChannelHandler.Sharable
@RequiredArgsConstructor
class NettyWSServerHandlerImpl extends SimpleChannelInboundHandler<TextWebSocketFrame> implements NettyWSServerHandler {

    private static final AttributeKey<String> CLIENT_ID_ATTR = AttributeKey.valueOf("clientId");

    private final WSService wsService;
    private final GsonProvider gsonProvider;

    /**
     * The authoritative channel per clientId.
     * Updated on CONNECT/RECONNECT. All server-to-client writes use this channel,
     * not the channel of the incoming message – these can differ in Netty when
     * multiple channels exist for the same client.
     */
    private final Map<String, NettyWSResponseEmitter> emitterByClientId = new ConcurrentHashMap<>();

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, TextWebSocketFrame frame) {
        try {
            var json = gsonProvider.getGson().fromJson(frame.text(), com.google.gson.JsonObject.class);
            var clientIdEl = json.get("clientId");
            if (clientIdEl == null) {
                log.warn("channelRead0: message without clientId – ignored");
                return;
            }
            var clientId = clientIdEl.getAsString();
            var requestType = json.has("request-type") ? json.get("request-type").getAsString() : null;

            // On CONNECT/RECONNECT: bind this channel as the authoritative one
            if ("connect".equalsIgnoreCase(requestType) || "reconnect".equalsIgnoreCase(requestType)) {
                ctx.channel().attr(CLIENT_ID_ATTR).set(clientId);
                var emitter = NettyWSResponseEmitter.of(ctx, gsonProvider.getGson());
                emitterByClientId.put(clientId, emitter);
                log.debug("channelRead0: registered channel for clientId={}", clientId);
                wsService.processRequest(frame.text(), emitter);
            } else {
                // For all other messages: use the registered emitter, not the incoming channel
                var emitter = emitterByClientId.get(clientId);
                if (emitter == null) {
                    // No registered channel yet – fallback to current channel
                    log.debug("channelRead0: no registered channel for clientId={}, using current", clientId);
                    emitter = NettyWSResponseEmitter.of(ctx, gsonProvider.getGson());
                }
                wsService.processRequest(frame.text(), emitter);
            }
        } catch (Exception e) {
            log.error("channelRead0: error processing message: {}", e.getMessage(), e);
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        var clientId = ctx.channel().attr(CLIENT_ID_ATTR).get();
        if (clientId != null) {
            var registered = emitterByClientId.get(clientId);
            if (registered != null && registered.isChannel(ctx.channel())) {
                emitterByClientId.remove(clientId);
                wsService.unregisterSession(clientId, ctx.channel());
                log.debug("channelInactive: unregistered clientId={}", clientId);
            } else {
                log.debug("channelInactive: stale channel for clientId={} – ignored", clientId);
            }
        }
        super.channelInactive(ctx);
    }
}
