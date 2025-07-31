package one.xis.http.client;

public interface HttpClientFactory {

    HttpClient createHttpClient(String serverUrl);

    HttpClient createHttpClient();
}
