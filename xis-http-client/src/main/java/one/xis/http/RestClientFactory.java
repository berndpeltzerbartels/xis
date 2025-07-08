package one.xis.http;

import one.xis.server.UrlHolder;

public interface RestClientFactory {

    RestClient createRestClient(UrlHolder serverUrl) throws HttpClientException;
}
