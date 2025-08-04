package one.xis.boot.netty;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.cookie.DefaultCookie;
import io.netty.handler.codec.http.cookie.ServerCookieEncoder;
import one.xis.http.ContentType;
import one.xis.http.HttpResponse;

import java.nio.charset.StandardCharsets;
import java.time.Duration;

public class NettyHttpResponse implements HttpResponse {

    private HttpResponseStatus status = HttpResponseStatus.OK;
    private ByteBuf content = Unpooled.EMPTY_BUFFER;
    private final HttpHeaders headers = new DefaultHttpHeaders();

    public FullHttpResponse getFullHttpResponse() {
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, status, content);
        response.headers().set(this.headers);
        if (!response.headers().contains(HttpHeaderNames.CONTENT_LENGTH)) {
            response.headers().set(HttpHeaderNames.CONTENT_LENGTH, content.readableBytes());
        }
        return response;
    }

    @Override
    public void setStatusCode(int statusCode) {
        this.status = HttpResponseStatus.valueOf(statusCode);
    }

    @Override
    public void setBody(String body) {
        this.content = Unpooled.copiedBuffer(body, StandardCharsets.UTF_8);
    }

    @Override
    public void setBody(byte[] body) {
        this.content = Unpooled.wrappedBuffer(body);
    }

    @Override
    public void setContentType(ContentType contentType) {
        if (contentType != null) {
            headers.set(HttpHeaderNames.CONTENT_TYPE, contentType.getValue());
        }
    }

    @Override
    public void setContentLength(int contentLength) {
        headers.set(HttpHeaderNames.CONTENT_LENGTH, contentLength);
    }

    @Override
    public Integer getStatusCode() {
        return status.code();
    }

    @Override
    public ContentType getContentType() {
        String contentTypeHeader = headers.get(HttpHeaderNames.CONTENT_TYPE);
        if (contentTypeHeader == null) {
            return null;
        }
        return ContentType.fromValue(contentTypeHeader);
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
        headers.add(HttpHeaderNames.SET_COOKIE, ServerCookieEncoder.STRICT.encode(cookie));
    }

    @Override
    public void sendRedirect(String location) {
        setStatusCode(HttpResponseStatus.FOUND.code());
        headers.set(HttpHeaderNames.LOCATION, location);
    }
}