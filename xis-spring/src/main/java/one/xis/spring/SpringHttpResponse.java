package one.xis.spring;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import one.xis.http.ContentType;
import one.xis.http.HttpResponse;
import org.springframework.http.ResponseCookie;

import java.io.IOException;
import java.time.Duration;

@RequiredArgsConstructor
public class SpringHttpResponse implements HttpResponse {
    private final HttpServletResponse response;

    @Override
    public void setStatusCode(int statusCode) {
        response.setStatus(statusCode);
    }

    @Override
    public void setBody(String body) {
        try {
            response.getWriter().write(body);
        } catch (IOException e) {
            throw new RuntimeException("Could not write response body", e);
        }
    }

    @Override
    public void setBody(byte[] body) {
        try {
            response.getOutputStream().write(body);
        } catch (IOException e) {
            throw new RuntimeException("Could not write response body", e);
        }
    }

    @Override
    public void setContentType(ContentType contentType) {
        if (contentType != null) {
            response.setContentType(contentType.getValue());
        }
    }

    @Override
    public void setContentLength(int contentLength) {
        response.setContentLength(contentLength);
    }

    @Override
    public Integer getStatusCode() {
        return response.getStatus();
    }

    @Override
    public ContentType getContentType() {
        String contentTypeHeader = response.getContentType();
        if (contentTypeHeader == null) {
            return null;
        }
        return ContentType.fromValue(contentTypeHeader);
    }

    @Override
    public void addHeader(String name, String value) {
        response.addHeader(name, value);
    }

    @Override
    public void addSecureCookie(String name, String value, Duration maxAge) {
        var cookie = ResponseCookie.from(name, value)
                .httpOnly(true)
                .secure(true)
                .sameSite("Lax")
                .maxAge(maxAge)
                .path("/")
                .build();
        response.addHeader("Set-Cookie", cookie.toString());
    }
}