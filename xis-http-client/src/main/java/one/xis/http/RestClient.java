package one.xis.http;

public interface RestClient {

    <R> R get(String url, Class<R> responseType) throws HttpClientException;

    <R> R post(String url, Object requestBody, Class<R> responseType) throws HttpClientException;

    HttpClient getHttpClient();
}
