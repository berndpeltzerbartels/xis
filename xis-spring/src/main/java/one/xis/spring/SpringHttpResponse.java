package one.xis.spring;

import jakarta.servlet.http.HttpServletResponse;
import one.xis.http.ContentType;
import one.xis.http.HttpResponse;
import org.springframework.http.ResponseCookie;

import java.io.IOException;
import java.time.Duration;

public class SpringHttpResponse implements HttpResponse {
    private int statusCode;
    private final HttpServletResponse response;
    private final boolean secureRequest;

    public SpringHttpResponse(HttpServletResponse response) {
        this(response, false);
    }

    /**
     * Creates a response bound to the transport security of the current request.
     * <p>
     * The flag controls only the {@code Secure} cookie attribute. It lets local
     * HTTP development logins work in Safari while preserving secure cookies for
     * HTTPS requests. Safari rejects token cookies marked {@code Secure} on
     * {@code http://localhost}, which otherwise makes a successful login look
     * like an immediate logout. In production HTTPS, the flag remains
     * {@code true}, so this is not a security relaxation for deployed systems.
     */
    public SpringHttpResponse(HttpServletResponse response, boolean secureRequest) {
        this.response = response;
        this.secureRequest = secureRequest;
    }

    @Override
    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
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
                .secure(secureRequest)
                .sameSite("Lax")
                .maxAge(maxAge)
                .path("/")
                .build();
        response.addHeader("Set-Cookie", cookie.toString());
    }

    @Override
    public void sendRedirect(String location) {
        try {
            response.sendRedirect(location);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void commitStatusCode() {
        response.setStatus(statusCode);
    }
}
