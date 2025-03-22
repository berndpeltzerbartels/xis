package one.xis.boot.netty;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import lombok.RequiredArgsConstructor;
import one.xis.context.XISComponent;
import one.xis.server.ClientRequest;
import one.xis.server.FrontendService;
import org.tinylog.Logger;

import java.io.IOException;
import java.util.Locale;


@ChannelHandler.Sharable
@XISComponent
@RequiredArgsConstructor
class NettyServerHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

    private final FrontendService frontendService;
    private final NettyMapper mapper;
    
    @Override
    public void handlerAdded(ChannelHandlerContext ctx) {
        Logger.info("Handler added: " + ctx.channel().remoteAddress());
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) {
        Logger.info("Handler removed: " + ctx.channel().remoteAddress());
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        Logger.info("Channel active: " + ctx.channel().remoteAddress());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        Logger.info("Channel inactive: " + ctx.channel().remoteAddress());
    }


    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        Logger.error(cause);
        ctx.close();
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest request) {
        Logger.info("Received request: " + request.uri());
        String uri = request.uri();
        HttpMethod method = request.method();
        FullHttpResponse response;

        if (method.equals(HttpMethod.GET)) {
            response = handleGetRequest(uri, request);
        } else if (method.equals(HttpMethod.POST)) {
            response = handlePostRequest(uri, request);
        } else {
            response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.METHOD_NOT_ALLOWED);
        }
        HttpUtil.setContentLength(response, response.content().readableBytes());
        Logger.info("Sending response: " + response.status());
        ctx.write(response);
        Logger.info("Flushing response");
        ctx.flush();
        Logger.info("Response flushed");
        ctx.close();  // Close the connection after sending the response
    }

    private FullHttpResponse handleGetRequest(String uri, FullHttpRequest request) {
        if (uri.equals("/") || uri.isEmpty() || uri.endsWith(".html")) {
            return createHtmlResponse(frontendService.getRootPageHtml());
        }
        return switch (uri) {
            case "/xis/config" -> createResponse(frontendService.getConfig());
            case "/xis/page" -> createHtmlResponse(frontendService.getPage(request.headers().get("uri")));
            case "/xis/page/head" -> createHtmlResponse(frontendService.getPageHead(request.headers().get("uri")));
            case "/xis/page/body" -> createHtmlResponse(frontendService.getPageBody(request.headers().get("uri")));
            case "/xis/page/body-attributes" ->
                    createResponse(frontendService.getBodyAttributes(request.headers().get("uri")));
            case "/xis/widget/html" -> createHtmlResponse(frontendService.getWidgetHtml(request.headers().get("uri")));
            case "/app.js" -> createHtmlResponse(frontendService.getAppJs());
            case "/classes.js" -> createHtmlResponse(frontendService.getClassesJs());
            case "/main.js" -> createHtmlResponse(frontendService.getMainJs());
            case "/functions.js" -> createHtmlResponse(frontendService.getFunctionsJs());
            default -> notFound(HttpMethod.GET, uri);
        };
    }

    private FullHttpResponse handlePostRequest(String uri, FullHttpRequest request) {
        ClientRequest clientRequest = parseRequest(request);
        clientRequest.setLocale(Locale.getDefault());
        return switch (uri) {
            case "/xis/page/model", "/xis/widget/model", "/xis/form/model" ->
                    createResponse(frontendService.processModelDataRequest(clientRequest));
            case "/xis/page/action", "/xis/form/action" ->
                    createResponse(frontendService.processActionRequest(clientRequest));
            default -> notFound(HttpMethod.POST, uri);
        };
    }

    private FullHttpResponse notFound(HttpMethod method, String uri) {
        return new DefaultFullHttpResponse(
                HttpVersion.HTTP_1_1,
                HttpResponseStatus.NOT_FOUND,
                io.netty.buffer.Unpooled.copiedBuffer("Not found " + method + " " + uri, java.nio.charset.StandardCharsets.UTF_8)); // create message
    }

    private FullHttpResponse createResponse(Object obj) {
        try {
            var response = mapper.toFullHttpResponse(obj);
            response.headers().set(HttpHeaderNames.CONTENT_TYPE, "application/json; charset=UTF-8");
            return response;
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
        var response = new DefaultFullHttpResponse(
                HttpVersion.HTTP_1_1,
                HttpResponseStatus.OK,
                io.netty.buffer.Unpooled.copiedBuffer(htmlContent, java.nio.charset.StandardCharsets.UTF_8)
        );
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/html; charset=UTF-8");
        return response;
    }
}