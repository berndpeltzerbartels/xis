package one.xis.boot.netty;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import one.xis.context.Component;
import one.xis.http.ContentType;
import one.xis.http.RestControllerService;
import one.xis.server.FrontendService;

import java.util.Optional;

@Log
@ChannelHandler.Sharable
@Component
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
            // Log the full exception with stack trace, not just the message
            log.severe("Error during request handling: " + e.getMessage());
            e.printStackTrace(); // Ensure stack trace is printed to stderr

            nettyResponse = createErrorResponse(e);
        }
        HttpUtil.setContentLength(nettyResponse, nettyResponse.content().readableBytes());
        ctx.writeAndFlush(nettyResponse);
        if (!HttpUtil.isKeepAlive(nettyRequest)) {
            ctx.close();
        }
    }

    /**
     * Handle channel exceptions that occur outside of the normal request processing flow.
     * This ensures that even uncaught exceptions are properly logged and don't silently fail.
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.severe("Uncaught exception in Netty channel: " + cause.getMessage());
        cause.printStackTrace(); // Ensure stack trace is printed
        ctx.close(); // Close the channel on uncaught exceptions
    }

    /**
     * Create a proper error response with details about the exception.
     * This provides better debugging information than a generic 500 error.
     */
    private FullHttpResponse createErrorResponse(Exception e) {
        var response = new NettyHttpResponse();
        response.setStatusCode(HttpResponseStatus.INTERNAL_SERVER_ERROR.code());
        response.setContentType(ContentType.TEXT_HTML);

        // Create a simple error page with exception details (only in development)
        String errorBody = createErrorPageHtml(e);
        response.setBody(errorBody);

        return response.getFullHttpResponse();
    }

    /**
     * Create an HTML error page with exception details.
     * This makes debugging much easier than a generic 500 error.
     */
    private String createErrorPageHtml(Exception e) {
        StringBuilder sb = new StringBuilder();
        sb.append("<!DOCTYPE html>");
        sb.append("<html><head><title>Internal Server Error</title></head>");
        sb.append("<body>");
        sb.append("<h1>Internal Server Error</h1>");
        sb.append("<h2>Exception: ").append(e.getClass().getSimpleName()).append("</h2>");
        sb.append("<p><strong>Message:</strong> ").append(escapeHtml(e.getMessage())).append("</p>");

        // Add stack trace for debugging (consider making this configurable for production)
        sb.append("<h3>Stack Trace:</h3>");
        sb.append("<pre>");
        for (StackTraceElement element : e.getStackTrace()) {
            sb.append(escapeHtml(element.toString())).append("\n");
        }
        sb.append("</pre>");

        sb.append("</body></html>");
        return sb.toString();
    }

    /**
     * Simple HTML escaping to prevent XSS in error messages.
     */
    private String escapeHtml(String text) {
        if (text == null) return "null";
        return text.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
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