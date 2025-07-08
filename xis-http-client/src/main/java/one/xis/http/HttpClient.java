package one.xis.http;

import java.util.Map;

public interface HttpClient {

    HttpResponse doGet(String url, Map<String, String> headers) throws HttpClientException;

    HttpResponse doPost(String url, String requestBody, Map<String, String> headers) throws HttpClientException;
    
}
