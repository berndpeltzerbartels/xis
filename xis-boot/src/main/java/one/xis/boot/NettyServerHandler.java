package one.xis.boot;

import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import lombok.RequiredArgsConstructor;
import one.xis.server.ClientConfig;
import one.xis.server.ClientRequest;
import one.xis.server.FrontendService;
import one.xis.server.ServerResponse;

import java.io.IOException;
import java.util.Locale;

@RequiredArgsConstructor
class NettyServerHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

    private final FrontendService frontendService;
    private final NettyMapper mapper;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest request) {
        String uri = request.uri();
        HttpMethod method = request.method();
        FullHttpResponse response;

        if (uri.equals("/") || uri.isEmpty() || uri.endsWith(".html")) {
            response = createHtmlResponse(frontendService.getRootPageHtml());
        } else if (uri.equals("/xis/config") && method.equals(HttpMethod.GET)) {
            ClientConfig config = frontendService.getConfig();
            response = createResponse(config);
        } else if (uri.equals("/xis/page/model") && method.equals(HttpMethod.POST)) {
            ClientRequest clientRequest = parseRequest(request);
            clientRequest.setLocale(Locale.getDefault());
            ServerResponse serverResponse = frontendService.processModelDataRequest(clientRequest);
            response = createResponse(serverResponse);
        } else {
            response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.NOT_FOUND);
        }

        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }

    private FullHttpResponse createResponse(ServerResponse serverResponse) {
        try {
            return mapper.toFullHttpResponse(serverResponse);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private FullHttpResponse createResponse(ClientConfig clientConfig) {
        try {
            return mapper.toFullHttpResponse(clientConfig);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private ClientRequest parseRequest(FullHttpRequest request) {
        try {
            return mapper.toClientRequest(request);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private FullHttpResponse createHtmlResponse(String htmlContent) {
        return new DefaultFullHttpResponse(
                HttpVersion.HTTP_1_1,
                HttpResponseStatus.OK,
                io.netty.buffer.Unpooled.copiedBuffer(htmlContent, java.nio.charset.StandardCharsets.UTF_8)
        );
    }
}