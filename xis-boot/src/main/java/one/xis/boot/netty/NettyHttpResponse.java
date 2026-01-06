package one.xis.boot.netty;

import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.*;
import one.xis.http.ContentType;
import one.xis.http.HttpResponse;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

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
    private final List<String> setCookieHeaders = new ArrayList<>();

    private boolean redirect;

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
        cookie.setSecure(true);
        cookie.setSameSite(cookie.sameSite()); // Should be Lax by default in modern Netty
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


    /**
     * Builds the Netty FullHttpResponse.
     * Called exactly once by NettyServerHandler.
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
            nettyHeaders.set(HttpHeaderNames.CONTENT_TYPE, contentType.toString());
        }

        nettyHeaders.set(HttpHeaderNames.CONTENT_LENGTH, payload.length);

        // Custom headers
        response.headers().set(this.headers);

        // Cookies
        for (String cookie : setCookieHeaders) {
            nettyHeaders.add(HttpHeaderNames.SET_COOKIE, cookie);
        }

        // Redirects should not be cached
        if (redirect) {
            nettyHeaders.set(HttpHeaderNames.CACHE_CONTROL, "no-store");
        }

        return response;
    }
}
