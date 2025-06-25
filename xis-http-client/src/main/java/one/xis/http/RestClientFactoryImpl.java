package one.xis.http;

import lombok.RequiredArgsConstructor;
import one.xis.context.XISComponent;

@XISComponent
@RequiredArgsConstructor
class RestClientFactoryImpl implements RestClientFactory {

    private final HttpClientFactory httpClientFactory;

    @Override
    public RestClient createRestClient(String serverUrl) throws HttpClientException {
        try {
            return new RestClientImpl(httpClientFactory.createHttpClient(serverUrl));
        } catch (Exception e) {
            throw new HttpClientException("Failed to create RestClient for " + serverUrl, e);
        }
    }
}
