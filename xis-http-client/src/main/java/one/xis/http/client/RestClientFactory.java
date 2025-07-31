package one.xis.http.client;

/**
 * Factory interface for creating instances of RestClient.
 * This allows for the creation of both a configured RestClient with a specific server URL and a default RestClient.
 */
public interface RestClientFactory {

    /**
     * Creates a RestClient instance configured with the specified server URL.
     *
     * @param serverUrl the URL of the server to connect to.
     * @return an instance of RestClient configured with the provided server URL.
     * @throws HttpClientException if there is an error creating the RestClient.
     */
    RestClient createRestClient(String serverUrl) throws HttpClientException;

    /**
     * Creates a default RestClient instance.
     *
     * @return an instance of RestClient with default configuration.
     * @throws HttpClientException if there is an error creating the RestClient.
     */
    RestClient createRestClient() throws HttpClientException;
}
