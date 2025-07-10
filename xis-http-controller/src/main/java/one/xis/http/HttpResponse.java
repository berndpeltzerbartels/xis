package one.xis.http;

public interface HttpResponse {
    void setStatusCode(int i);

    void setBody(String s);


    void setBody(byte[] body);

    void setContentType(ContentType contentType);

    Integer getStatusCode();

    ContentType getContentType();
}
