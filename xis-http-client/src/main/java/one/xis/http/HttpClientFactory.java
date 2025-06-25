package one.xis.http;

public interface HttpClientFactory {

    HttpClient createHttpClient(String serverUrl);
}
