package one.xis.http;

public interface RestClientFactory {

    RestClient createRestClient(String serverUrl) throws HttpClientException;
}
