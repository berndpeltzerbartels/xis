package one.xis.boot.netty;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import lombok.RequiredArgsConstructor;
import one.xis.context.XISComponent;
import one.xis.http.ContentType;
import one.xis.http.RestControllerService;
import one.xis.server.FrontendService;

import java.nio.charset.StandardCharsets;
import java.util.Optional;

@ChannelHandler.Sharable
@XISComponent
@RequiredArgsConstructor
public class NettyServerHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

    private final FrontendService frontendService;
    private final RestControllerService restControllerService;
    private final NettyResourceHandler resourceHandler;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest nettyRequest) {
        var request = new NettyHttpRequest(nettyRequest);
        var response = new NettyHttpResponse();

        FullHttpResponse nettyResponse;

        try {
            String uri = request.getPath();
            if (uri.equals("/") || uri.isEmpty() || uri.endsWith(".html")) {
                response.setContentType(ContentType.TEXT_HTML);
                response.setBody(frontendService.getRootPageHtml());
                nettyResponse = response.getFullHttpResponse();
            } else {
                restControllerService.doInvocation(request, response);
                // Prüfen, ob der RestControllerService die Anfrage nicht verarbeiten konnte (und 404 gesetzt hat)
                if (response.getStatusCode() != null && response.getStatusCode() == HttpResponseStatus.NOT_FOUND.code()) {
                    // Dann versuchen, als statische Ressource zu laden
                    Optional<FullHttpResponse> resourceResponse = resourceHandler.handle(uri);
                    // Wenn auch das fehlschlägt, die 404-Antwort vom RestController verwenden oder eine neue erstellen
                    nettyResponse = resourceResponse.orElseGet(response::getFullHttpResponse);
                } else {
                    // Der RestController hatte einen passenden Endpunkt
                    nettyResponse = response.getFullHttpResponse();
                }
            }
        } catch (Exception e) {
            nettyResponse = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.INTERNAL_SERVER_ERROR);
        }

        HttpUtil.setContentLength(nettyResponse, nettyResponse.content().readableBytes());
        ctx.writeAndFlush(nettyResponse);
        if (!HttpUtil.isKeepAlive(nettyRequest)) {
            ctx.close();
        }
    }

    private FullHttpResponse notFound(one.xis.http.HttpMethod method, String uri) {
        String body = "Not found " + method.name() + " " + uri;
        return new DefaultFullHttpResponse(
                HttpVersion.HTTP_1_1,
                HttpResponseStatus.NOT_FOUND,
                Unpooled.copiedBuffer(body, StandardCharsets.UTF_8)
        );
    }
}