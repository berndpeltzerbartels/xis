package one.xis.http;

import java.time.Duration;

public interface HttpResponse {
    void setStatusCode(int i);

    void setBody(byte[] body);

    default void setBody(String body) {
        setBody(body.getBytes());
    }

    void setContentType(ContentType contentType);

    Integer getStatusCode();

    ContentType getContentType();

    void addHeader(String name, String value);

    /**
     * Adds an HTTP-only authentication cookie.
     * <p>
     * Implementations must only add the {@code Secure} cookie attribute when
     * the browser-facing request scheme is HTTPS. Local development often uses
     * {@code http://localhost}; Safari rejects {@code Secure} cookies there,
     * which makes a successful login look like an immediate logout because the
     * access and refresh token cookies never reach the next request.
     * <p>
     * This does not weaken production authentication cookies. In HTTPS
     * deployments, including deployments behind a TLS-terminating reverse proxy
     * that forwards {@code X-Forwarded-Proto: https}, the framework still emits
     * token cookies with the {@code Secure} attribute. The relaxation applies
     * only to plain HTTP requests, primarily local development.
     *
     * @param name cookie name
     * @param value cookie value
     * @param maxAge cookie lifetime
     */
    void addSecureCookie(String name, String value, Duration maxAge);

    void sendRedirect(String location);
}
