package one.xis.boot.netty;

import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.*;
import one.xis.http.ContentType;
import one.xis.http.HttpResponse;

import java.time.Duration;

import static io.netty.handler.codec.http.cookie.CookieHeaderNames.SameSite.Lax;
import static io.netty.handler.codec.http.cookie.ServerCookieEncoder.STRICT;

/**
 * Netty-backed HttpResponse implementation.
 * <p>
 * Collects response data first and creates the Netty FullHttpResponse
 * exactly once at the end of request processing.
 */
public final class NettyHttpResponse implements HttpResponse {

    private Integer statusCode;
    private byte[] body;
    private ContentType contentType;

    private final HttpHeaders headers = new DefaultHttpHeaders();
    private final boolean secureRequest;

    private boolean redirect;
    private boolean handledExternally;

    /**
     * Creates a response bound to the transport security of the current request.
     * <p>
     * The flag is used only for cookie attributes. It must represent the
     * externally visible scheme, not just the local socket, so deployments behind
     * TLS-terminating proxies still emit {@code Secure} cookies when
     * {@code X-Forwarded-Proto} says {@code https}.
     * <p>
     * For plain {@code http://localhost}, especially in Safari, the flag must be
     * {@code false}; otherwise Safari drops authentication cookies marked
     * {@code Secure} and local login appears broken. This is safe for production
     * because HTTPS requests still pass {@code true}.
     */
    public NettyHttpResponse(boolean secureRequest) {
        this.secureRequest = secureRequest;
    }

    @Override
    public void setStatusCode(int code) {
        this.statusCode = code;
    }

    @Override
    public Integer getStatusCode() {
        return statusCode;
    }

    @Override
    public void setBody(byte[] body) {
        this.body = body;
    }

    @Override
    public void setContentType(ContentType contentType) {
        this.contentType = contentType;
        headers.add(HttpHeaderNames.CONTENT_TYPE, contentType.getValue());
    }

    @Override
    public ContentType getContentType() {
        return contentType;
    }

    @Override
    public void addHeader(String name, String value) {
        headers.add(name, value);
    }

    @Override
    public void addSecureCookie(String name, String value, Duration maxAge) {
        DefaultCookie cookie = new DefaultCookie(name, value);
        cookie.setHttpOnly(true);
        cookie.setSecure(secureRequest);
        cookie.setSameSite(Lax);
        cookie.setMaxAge(maxAge.getSeconds());
        cookie.setPath("/");
        headers.add(HttpHeaderNames.SET_COOKIE, STRICT.encode(cookie));
    }

    @Override
    public void sendRedirect(String location) {
        setStatusCode(HttpResponseStatus.FOUND.code());
        addHeader(HttpHeaderNames.LOCATION.toString(), location);
        redirect = true;
    }

    public void markHandledExternally() {
        handledExternally = true;
    }

    public boolean isHandledExternally() {
        return handledExternally;
    }


    /**
     * Builds the Netty FullHttpResponse.
     * Called exactly once by NettyHttpServerHandler.
     */
    public FullHttpResponse toNettyResponse() {
        HttpResponseStatus status = HttpResponseStatus.valueOf(
                statusCode != null ? statusCode : HttpResponseStatus.OK.code()
        );

        byte[] payload = body != null ? body : new byte[0];

        FullHttpResponse response = new DefaultFullHttpResponse(
                HttpVersion.HTTP_1_1,
                status,
                Unpooled.wrappedBuffer(payload)
        );

        HttpHeaders nettyHeaders = response.headers();

        if (contentType != null) {
            nettyHeaders.set(HttpHeaderNames.CONTENT_TYPE, contentType.getValue());
        }

        nettyHeaders.set(HttpHeaderNames.CONTENT_LENGTH, payload.length);

        // Custom headers
        response.headers().set(this.headers);

        // Redirects should not be cached
        if (redirect) {
            nettyHeaders.set(HttpHeaderNames.CACHE_CONTROL, "no-store");
        }

        return response;
    }
}
