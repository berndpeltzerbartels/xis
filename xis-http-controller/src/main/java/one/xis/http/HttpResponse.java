package one.xis.http;

import java.time.Duration;

public interface HttpResponse {
    void setStatusCode(int i);

    void setBody(String s);


    void setBody(byte[] body);

    void setContentType(ContentType contentType);

    void setContentLength(int contentLength);

    Integer getStatusCode();

    ContentType getContentType();

    void addHeader(String name, String value);

    void addSecureCookie(String name, String value, Duration maxAge);

    void sendRedirect(String location);
}
