package one.xis.ws;

import com.google.gson.Gson;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
class NettyWSResponseEmitter implements WSEmitter {
    private final Channel channel;
    private final Gson gson;

    static NettyWSResponseEmitter of(ChannelHandlerContext ctx, Gson gson) {
        return new NettyWSResponseEmitter(ctx.channel(), gson);
    }

    @Override
    public void send(String responseJson) {
        log.debug("send: channelId={} active={}", channel.id(), channel.isActive());
        if (!channel.isActive()) {
            log.warn("send: channelId={} is not active – message dropped", channel.id());
            return;
        }
        channel.eventLoop().execute(() -> {
            log.debug("send.execute: channelId={} active={}", channel.id(), channel.isActive());
            if (channel.isActive()) {
                channel.writeAndFlush(new TextWebSocketFrame(responseJson));
            } else {
                log.warn("send.execute: channelId={} became inactive before write – message dropped", channel.id());
            }
        });
    }

    @Override
    public void send(Object response) {
        send(gson.toJson(response));
    }

    @Override
    public boolean isOpen() {
        log.debug("isOpen: channelId={} active={}", channel.id(), channel.isActive());
        return channel.isActive();
    }

    @Override
    public void close() {
        log.debug("close: channelId={}", channel.id());
        channel.close();
    }

    @Override
    public boolean isChannel(Object channel) {
        var result = this.channel == channel;
        log.debug("isChannel: thisChannelId={} result={}", this.channel.id(), result);
        return result;
    }
}
