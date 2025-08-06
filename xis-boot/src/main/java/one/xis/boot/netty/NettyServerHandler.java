package one.xis.boot.netty;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import one.xis.context.XISComponent;
import one.xis.http.ContentType;
import one.xis.http.RestControllerService;
import one.xis.server.FrontendService;

import java.util.Optional;

@Log
@ChannelHandler.Sharable
@XISComponent
@RequiredArgsConstructor
public class NettyServerHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

    private final FrontendService frontendService;
    private final RestControllerService restControllerService;
    private final NettyResourceHandler resourceHandler;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest nettyRequest) {
        FullHttpResponse nettyResponse;
        try {
            nettyResponse = handleRequest(nettyRequest);
        } catch (Exception e) {
            log.severe("Error during request handling: " + e.getMessage());
            nettyResponse = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.INTERNAL_SERVER_ERROR);
        }
        HttpUtil.setContentLength(nettyResponse, nettyResponse.content().readableBytes());
        ctx.writeAndFlush(nettyResponse);
        if (!HttpUtil.isKeepAlive(nettyRequest)) {
            ctx.close();
        }
    }

    private FullHttpResponse handleRequest(FullHttpRequest nettyRequest) {
        var request = new NettyHttpRequest(nettyRequest);
        String uri = request.getPath();

        if (isFrontendRequest(uri)) {
            return handleFrontendRequest();
        }
        return handleApiOrStaticResourceRequest(request);
    }

    private boolean isFrontendRequest(String uri) {
        return uri.equals("/") || uri.isEmpty() || uri.endsWith(".html");
    }

    private FullHttpResponse handleFrontendRequest() {
        var response = new NettyHttpResponse();
        response.setContentType(ContentType.TEXT_HTML);
        response.setBody(frontendService.getRootPageHtml());
        return response.getFullHttpResponse();
    }

    private FullHttpResponse handleApiOrStaticResourceRequest(NettyHttpRequest request) {
        var response = new NettyHttpResponse();
        restControllerService.doInvocation(request, response);

        // Wenn der RestController keine Route gefunden hat (404), versuche statische Ressourcen.
        if (isNotFound(response)) {
            Optional<FullHttpResponse> resourceResponse = resourceHandler.handle(request.getPath());
            // Gib die Ressource zurück, oder die 404-Antwort, wenn nicht gefunden.
            return resourceResponse.orElseGet(response::getFullHttpResponse);
        }

        // Andernfalls hat der RestController die Anfrage erfolgreich bearbeitet.
        return response.getFullHttpResponse();
    }

    private boolean isNotFound(NettyHttpResponse response) {
        return response.getStatusCode() != null && response.getStatusCode() == HttpResponseStatus.NOT_FOUND.code();
    }
}