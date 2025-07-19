package one.xis.context;

import lombok.Getter;
import one.xis.http.ContentType;
import one.xis.http.HttpResponse;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
public class HttpTestResponse implements HttpResponse {

    private Integer statusCode;
    private String body;
    private byte[] bodyBytes;
    private ContentType contentType;
    private int contentLength;
    private String redirectLocation;
    private final Map<String, List<String>> headers = new HashMap<>();

    @Override
    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    @Override
    public void setBody(String body) {
        this.body = body;
    }

    @Override
    public void setBody(byte[] body) {
        this.bodyBytes = body;
    }

    @Override
    public void setContentType(ContentType contentType) {
        this.contentType = contentType;
    }

    @Override
    public void setContentLength(int contentLength) {
        this.contentLength = contentLength;
    }

    @Override
    public void addHeader(String name, String value) {
        headers.computeIfAbsent(name, k -> new ArrayList<>()).add(value);
    }

    @Override
    public void addSecureCookie(String name, String value, Duration maxAge) {
        // Für Testzwecke wird dies oft vereinfacht.
        // Hier wird ein einfacher "Set-Cookie"-Header hinzugefügt.
        String cookieValue = String.format("%s=%s; Max-Age=%d; Secure; HttpOnly", name, value, maxAge.getSeconds());
        addHeader("Set-Cookie", cookieValue);
    }

    @Override
    public void sendRedirect(String location) {
        this.redirectLocation = location;
        setStatusCode(302); // Status für "Found" (temporäre Weiterleitung)
        addHeader("Location", location);
    }
}