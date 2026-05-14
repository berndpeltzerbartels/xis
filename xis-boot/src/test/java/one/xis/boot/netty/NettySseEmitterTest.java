package one.xis.boot.netty;

import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.codec.http.DefaultHttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class NettySseEmitterTest {

    @Test
    void sendsBrowserFriendlySseHeaders() {
        var channel = new EmbeddedChannel();

        new NettySseEmitter(channel, null);

        var response = (DefaultHttpResponse) channel.readOutbound();
        assertThat(response.headers().get(HttpHeaderNames.CONTENT_TYPE)).isEqualTo("text/event-stream;charset=UTF-8");
        assertThat(response.headers().get(HttpHeaderNames.CACHE_CONTROL)).isEqualTo("no-cache");
        assertThat(response.headers().get(HttpHeaderNames.CONNECTION)).isEqualTo(HttpHeaderValues.KEEP_ALIVE.toString());
        assertThat(response.headers().get("Keep-Alive")).isEqualTo("timeout=60");
        assertThat(response.headers().get("X-Accel-Buffering")).isEqualTo("no");
        assertThat(response.headers().get(HttpHeaderNames.DATE)).isNotBlank();
        assertThat(response.headers().get(HttpHeaderNames.TRANSFER_ENCODING)).isEqualTo(HttpHeaderValues.CHUNKED.toString());
    }
}
