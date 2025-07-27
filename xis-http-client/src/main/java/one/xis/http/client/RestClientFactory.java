package one.xis.http.client;

public interface RestClientFactory {

    RestClient createRestClient(String serverUrl) throws HttpClientException;
}
