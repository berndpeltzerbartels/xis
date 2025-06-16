package one.xis.boot.netty;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import lombok.RequiredArgsConstructor;
import one.xis.context.XISComponent;
import one.xis.server.ClientRequest;
import one.xis.server.FrontendService;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;

@ChannelHandler.Sharable
@XISComponent
@RequiredArgsConstructor
public class NettyServerHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

    private final FrontendService frontendService;
    private final NettyController controller;
    private final NettyMapper mapper;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest request) {
        String uri = new QueryStringDecoder(request.uri()).path();
        HttpMethod method = request.method();
        FullHttpResponse response;

        try {
            if (method.equals(HttpMethod.GET)) {
                response = handleGetRequest(uri, request);
            } else if (method.equals(HttpMethod.POST)) {
                response = handlePostRequest(uri, request);
            } else {
                response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.METHOD_NOT_ALLOWED);
            }
        } catch (Exception e) {
            response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.INTERNAL_SERVER_ERROR);
        }

        HttpUtil.setContentLength(response, response.content().readableBytes());
        ctx.writeAndFlush(response);
        if (!HttpUtil.isKeepAlive(request)) {
            ctx.close();
        }
    }

    private FullHttpResponse handleGetRequest(String uri, FullHttpRequest request) throws IOException {
        if (uri.equals("/") || uri.endsWith(".html")) {
            return mapper.toFullHttpResponse(frontendService.getRootPageHtml());
        }
        if (uri.startsWith("/xis/auth/")) {
            String provider = uri.substring("/xis/auth/".length());
            return controller.authenticationCallback(request, provider);
        }
        var token = extractBearerToken(request);
        return switch (uri) {
            case "/xis/config" -> mapper.toFullHttpResponse(controller.getComponentConfig());
            case "/xis/page" -> mapper.toFullHttpResponse(controller.getPage(request.headers().get("uri")));
            case "/xis/page/head" -> mapper.toFullHttpResponse(controller.getPageHead(request.headers().get("uri")));
            case "/xis/page/body" -> mapper.toFullHttpResponse(controller.getPageBody(request.headers().get("uri")));
            case "/xis/page/body-attributes" ->
                    mapper.toFullHttpResponse(controller.getBodyAttributes(request.headers().get("uri")));
            case "/xis/widget/html" ->
                    mapper.toFullHttpResponse(controller.getWidgetHtml(request.headers().get("uri")));
            case "/app.js" -> mapper.toFullHttpResponse(controller.getAppJs());
            case "/classes.js" -> mapper.toFullHttpResponse(controller.getClassesJs());
            case "/main.js" -> mapper.toFullHttpResponse(controller.getMainJs());
            case "/functions.js" -> mapper.toFullHttpResponse(controller.getFunctionsJs());
            case "/bundle.min.js" -> mapper.toFullHttpResponse(controller.getBundleJs());
            default -> notFound(HttpMethod.GET, uri);
        };
    }

    private FullHttpResponse handlePostRequest(String uri, FullHttpRequest request) throws IOException {
        ClientRequest clientRequest = mapper.toClientRequest(request);
        clientRequest.setLocale(Locale.getDefault());
        var token = extractBearerToken(request);
        return switch (uri) {
            case "/xis/page/model" -> controller.getPageModel(clientRequest, token, clientRequest.getLocale());
            case "/xis/form/model" -> controller.getFormModel(clientRequest, token, clientRequest.getLocale());
            case "/xis/widget/model" -> controller.getWidgetModel(clientRequest, token, clientRequest.getLocale());
            case "/xis/page/action" -> controller.onPageLinkAction(clientRequest, token, clientRequest.getLocale());
            case "/xis/form/action" -> controller.onFormAction(clientRequest, token, clientRequest.getLocale());
            case "/xis/widget/action" -> controller.onWidgetLinkAction(clientRequest, token, clientRequest.getLocale());
            case "/xis/token/renew" -> {
                String header = request.headers().get("Authentication");
                if (header == null || !header.startsWith("Bearer ")) yield unauthorized();
                yield controller.renewApiTokens(header.substring("Bearer ".length()));
            }
            case "/xis/idp/tokens" -> {
                QueryStringDecoder decoder = new QueryStringDecoder(uri);
                String code = decoder.parameters().getOrDefault("code", List.of()).stream().findFirst().orElse(null);
                String state = decoder.parameters().getOrDefault("state", List.of()).stream().findFirst().orElse(null);
                yield controller.idpGetTokens(code, state);
            }
            default -> notFound(HttpMethod.POST, uri);
        };
    }

    private FullHttpResponse notFound(HttpMethod method, String uri) {
        return new DefaultFullHttpResponse(
                HttpVersion.HTTP_1_1,
                HttpResponseStatus.NOT_FOUND,
                Unpooled.copiedBuffer("Not found " + method + " " + uri, StandardCharsets.UTF_8)
        );
    }

    private FullHttpResponse unauthorized() {
        String responseBody = "Unauthorized";
        byte[] content = responseBody.getBytes(StandardCharsets.UTF_8);

        FullHttpResponse response = new DefaultFullHttpResponse(
                HttpVersion.HTTP_1_1,
                HttpResponseStatus.UNAUTHORIZED,
                Unpooled.wrappedBuffer(content)
        );

        response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/plain; charset=UTF-8");
        response.headers().set(HttpHeaderNames.CONTENT_LENGTH, content.length);

        return response;
    }

    public static String extractBearerToken(FullHttpRequest request) {
        String authHeader = request.headers().get(HttpHeaderNames.AUTHORIZATION);
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring("Bearer ".length());
        }
        return null;
    }


}
