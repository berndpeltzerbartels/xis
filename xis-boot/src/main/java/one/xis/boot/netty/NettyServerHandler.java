package one.xis.boot.netty;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import one.xis.context.Component;
import one.xis.http.ContentType;
import one.xis.http.RestControllerService;
import one.xis.server.FrontendService;

import java.nio.charset.StandardCharsets;

import static io.netty.handler.codec.http.HttpHeaderNames.*;
import static io.netty.handler.codec.http.HttpHeaderValues.NO_STORE;

@Slf4j
@ChannelHandler.Sharable
@Component
@RequiredArgsConstructor
public class NettyServerHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

    private final FrontendService frontendService;
    private final RestControllerService restControllerService;
    private final NettyResourceHandler resourceHandler;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest nettyRequest) {
        FullHttpResponse response;
        try {
            response = routeRequest(nettyRequest, ctx);
            writeResponse(ctx, nettyRequest, response);
        } catch (Throwable t) {
            if (isFatal(t)) {
                // log once and terminate process - systemd will handle restart policy
                log.error("Fatal error in request handling, terminating JVM", t);
                System.exit(1);
                return;
            }

            log.error("Request handling failed: {}", t.getMessage(), t);
            FullHttpResponse errorResponse = createInternalServerError(t);
            writeResponse(ctx, nettyRequest, errorResponse);
        }
    }

    private boolean isFatal(Throwable t) {
        // unwrap common wrappers
        Throwable root = unwrap(t);

        return root instanceof LinkageError
                || root instanceof VirtualMachineError; // optional custom
    }

    private Throwable unwrap(Throwable t) {
        Throwable cur = t;
        while (cur.getCause() != null && cur.getCause() != cur) {
            cur = cur.getCause();
        }
        return cur;
    }


    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error("Uncaught channel exception: " + cause.getMessage(), cause);
        ctx.close();
    }

    private FullHttpResponse routeRequest(FullHttpRequest nettyRequest, ChannelHandlerContext ctx) {
        NettyHttpRequest request = new NettyHttpRequest(nettyRequest, ctx);
        String path = request.getPath();
        if (isFrontendRequest(path)) {
            return handleFrontendRequest();
        }

        return handleApiOrStaticResourceRequest(request);
    }

    private boolean isFrontendRequest(String path) {
        return path.equals("/") || path.isEmpty() || path.endsWith(".html");
    }

    private FullHttpResponse handleFrontendRequest() {
        String html = frontendService.getRootPageHtml();
        return okHtml(html);
    }

    private FullHttpResponse handleApiOrStaticResourceRequest(NettyHttpRequest request) {
        // Keep this usage as required.
        NettyHttpResponse response = new NettyHttpResponse();
        restControllerService.doInvocation(request, response);

        if (isNotFound(response)) {
            return notFound();
        }

        return response.toNettyResponse();
    }

    private boolean isNotFound(NettyHttpResponse response) {
        Integer status = response.getStatusCode();
        return status != null && status == HttpResponseStatus.NOT_FOUND.code();
    }

    private void writeResponse(ChannelHandlerContext ctx, FullHttpRequest req, FullHttpResponse res) {
        ensureDateAndServerHeaders(res);
        ensureContentLength(res);

        boolean keepAlive = HttpUtil.isKeepAlive(req);
        if (keepAlive) {
            HttpUtil.setKeepAlive(res, true);
        } else {
            HttpUtil.setKeepAlive(res, false);
        }

        var writeFuture = ctx.writeAndFlush(res);

        if (!keepAlive) {
            writeFuture.addListener(ChannelFutureListener.CLOSE);
        }
    }

    private void ensureDateAndServerHeaders(FullHttpResponse res) {
        // Keep minimal, avoid allocating lots of objects per request.
        if (!res.headers().contains(SERVER)) {
            res.headers().set(SERVER, "xis-netty");
        }
    }

    private void ensureContentLength(FullHttpResponse res) {
        // If upstream forgot it, set it.
        if (!res.headers().contains(CONTENT_LENGTH)) {
            HttpUtil.setContentLength(res, res.content().readableBytes());
        }
    }

    private FullHttpResponse okHtml(String html) {
        byte[] bytes = html.getBytes(StandardCharsets.UTF_8);
        FullHttpResponse response = new DefaultFullHttpResponse(
                HttpVersion.HTTP_1_1,
                HttpResponseStatus.OK,
                Unpooled.wrappedBuffer(bytes)
        );

        response.headers().set(CONTENT_TYPE, ContentType.TEXT_HTML_UTF8.toString());
        response.headers().set(CONTENT_LENGTH, bytes.length);
        return response;
    }

    private FullHttpResponse createInternalServerError(Throwable e) {
        String body = buildSafeErrorPage(e);
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);

        FullHttpResponse response = new DefaultFullHttpResponse(
                HttpVersion.HTTP_1_1,
                HttpResponseStatus.INTERNAL_SERVER_ERROR,
                Unpooled.wrappedBuffer(bytes)
        );

        response.headers().set(CONTENT_TYPE, ContentType.TEXT_HTML_UTF8.toString());
        response.headers().set(CONTENT_LENGTH, bytes.length);
        response.headers().set(CACHE_CONTROL, NO_STORE);
        return response;
    }

    private FullHttpResponse notFound() {
        byte[] body = "Not Found".getBytes(java.nio.charset.StandardCharsets.UTF_8);

        FullHttpResponse res = new DefaultFullHttpResponse(
                HttpVersion.HTTP_1_1,
                HttpResponseStatus.NOT_FOUND,
                Unpooled.wrappedBuffer(body)
        );

        res.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/plain; charset=utf-8");
        res.headers().setInt(HttpHeaderNames.CONTENT_LENGTH, body.length);
        return res;
    }


    private String buildSafeErrorPage(Throwable e) {
        // Keep it short to reduce allocations and avoid leaking internal details in production.
        String message = e.getMessage() == null ? "" : escapeHtml(e.getMessage());

        return """
                <!DOCTYPE html>
                <html>
                  <head><meta charset="utf-8"><title>Internal Server Error</title></head>
                  <body>
                    <h1>Internal Server Error</h1>
                    <p>%s</p>
                  </body>
                </html>
                """.formatted(message);
    }

    private String escapeHtml(String text) {
        return text.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}
