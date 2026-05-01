package one.xis.boot.netty;

import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.handler.codec.http.*;
import lombok.extern.slf4j.Slf4j;
import one.xis.http.SseEmitter;

import java.nio.charset.StandardCharsets;

@Slf4j
class NettySseEmitter implements SseEmitter {

    private final Channel channel;

    NettySseEmitter(Channel channel) {
        this.channel = channel;
        sendSseHeaders();
    }

    @Override
    public void send(String data) {
        if (!isOpen()) {
            log.warn("send: channel is not active - message dropped");
            return;
        }
        byte[] bytes = data.getBytes(StandardCharsets.UTF_8);
        var buf = Unpooled.wrappedBuffer(bytes);
        var chunk = new DefaultHttpContent(buf);
        channel.writeAndFlush(chunk).addListener(future -> {
            if (!future.isSuccess()) {
                log.warn("send: write failed - {}", future.cause().getMessage());
            }
        });
    }

    @Override
    public void close() {
        if (channel.isActive()) {
            channel.writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT)
                    .addListener(ChannelFutureListener.CLOSE);
        }
    }

    @Override
    public boolean isOpen() {
        return channel.isActive();
    }

    // -------------------------------------------------------------------------

    private void sendSseHeaders() {
        HttpResponse response = new DefaultHttpResponse(
                HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/event-stream; charset=UTF-8");
        response.headers().set(HttpHeaderNames.CACHE_CONTROL, "no-cache");
        response.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
        response.headers().set(HttpHeaderNames.TRANSFER_ENCODING, HttpHeaderValues.CHUNKED);
        channel.writeAndFlush(response);
    }
}
