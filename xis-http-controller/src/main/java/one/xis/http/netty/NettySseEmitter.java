package one.xis.http.netty;

import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.handler.codec.http.*;
import one.xis.http.SseEmitter;
import one.xis.http.SseSendFailedException;

import java.nio.charset.StandardCharsets;
import java.time.ZonedDateTime;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import static java.time.ZoneOffset.UTC;
import static java.time.format.DateTimeFormatter.RFC_1123_DATE_TIME;

class NettySseEmitter implements SseEmitter {

    private final Channel channel;

    NettySseEmitter(Channel channel, String origin) {
        this.channel = channel;
        sendSseHeaders(origin);
    }

    @Override
    public CompletionStage<Void> send(String data) {
        if (!isOpen()) {
            return CompletableFuture.failedFuture(new SseSendFailedException("SSE channel is not active"));
        }
        byte[] bytes = data.getBytes(StandardCharsets.UTF_8);
        var buf = Unpooled.wrappedBuffer(bytes);
        var chunk = new DefaultHttpContent(buf);
        CompletableFuture<Void> result = new CompletableFuture<>();
        channel.writeAndFlush(chunk).addListener(future -> {
            if (future.isSuccess()) {
                result.complete(null);
            } else {
                result.completeExceptionally(new SseSendFailedException("SSE write failed", future.cause()));
            }
        });
        return result;
    }

    @Override
    public void close() {
        if (channel.isActive()) {
            if (channel.isWritable()) {
                channel.writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT)
                        .addListener(ChannelFutureListener.CLOSE);
            } else {
                channel.close();
            }
        }
    }

    @Override
    public boolean isOpen() {
        return channel.isActive();
    }

    // -------------------------------------------------------------------------

    private void sendSseHeaders(String origin) {
        HttpResponse response = new DefaultHttpResponse(
                HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/event-stream;charset=UTF-8");
        response.headers().set(HttpHeaderNames.CACHE_CONTROL, "no-cache");
        response.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
        response.headers().set("Keep-Alive", "timeout=60");
        response.headers().set("X-Accel-Buffering", "no");
        response.headers().set(HttpHeaderNames.DATE, RFC_1123_DATE_TIME.format(ZonedDateTime.now(UTC)));
        response.headers().set(HttpHeaderNames.TRANSFER_ENCODING, HttpHeaderValues.CHUNKED);
        if (origin != null && !origin.isBlank()) {
            response.headers().set(HttpHeaderNames.ACCESS_CONTROL_ALLOW_ORIGIN, origin);
            response.headers().set(HttpHeaderNames.ACCESS_CONTROL_ALLOW_CREDENTIALS, "true");
            response.headers().add(HttpHeaderNames.VARY, HttpHeaderNames.ORIGIN);
        }
        channel.writeAndFlush(response);
    }
}
