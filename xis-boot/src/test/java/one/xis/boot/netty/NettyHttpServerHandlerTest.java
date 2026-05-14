package one.xis.boot.netty;

import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.DefaultHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.HttpResponseStatus;
import one.xis.http.ContentType;
import one.xis.http.RestControllerService;
import one.xis.server.FrontendService;
import one.xis.server.LocalUrlHolder;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;

class NettyHttpServerHandlerTest {

    @Test
    void externallyHandledStreamingResponseIsNotWrittenAgain() {
        var restControllerService = mock(RestControllerService.class);
        doAnswer(invocation -> {
            NettyHttpRequest request = invocation.getArgument(0);
            NettyHttpResponse response = invocation.getArgument(1);
            request.getChannel().writeAndFlush(new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK));
            response.setStatusCode(200);
            response.markHandledExternally();
            return null;
        }).when(restControllerService).doInvocation(any(), any());

        var handler = new NettyHttpServerHandler(
                mock(FrontendService.class),
                restControllerService,
                mock(NettyResourceHandler.class),
                mock(LocalUrlHolder.class)
        );
        var channel = new EmbeddedChannel(handler);
        var request = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, "/xis/events?clientId=test");

        channel.writeInbound(request);

        Object streamingResponse = channel.readOutbound();
        Object extraResponse = channel.readOutbound();
        assertThat(streamingResponse).isInstanceOf(DefaultHttpResponse.class);
        assertThat(extraResponse).isNull();
    }

    @Test
    void internalServerErrorFallbackUsesJsonErrorFormat() throws Exception {
        var handler = new NettyHttpServerHandler(
                mock(FrontendService.class),
                mock(RestControllerService.class),
                mock(NettyResourceHandler.class),
                mock(LocalUrlHolder.class)
        );

        FullHttpResponse response = invokeCreateInternalServerError(handler,
                new IllegalStateException("Cannot read field \"next\" because \"this.next\" is null"));

        var body = response.content().toString(StandardCharsets.UTF_8);
        assertThat(response.status()).isEqualTo(HttpResponseStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.headers().get(HttpHeaderNames.CONTENT_TYPE)).isEqualTo(ContentType.JSON_UTF8.getValue());
        assertThat(body).isEqualTo("{\"message\":\"Cannot read field \\\"next\\\" because \\\"this.next\\\" is null\"}");
    }

    private FullHttpResponse invokeCreateInternalServerError(NettyHttpServerHandler handler, Throwable throwable)
            throws Exception {
        Method method = NettyHttpServerHandler.class.getDeclaredMethod("createInternalServerError", Throwable.class);
        method.setAccessible(true);
        return (FullHttpResponse) method.invoke(handler, throwable);
    }
}
