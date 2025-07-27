package one.xis.http.client;

import com.google.gson.Gson;
import lombok.RequiredArgsConstructor;
import one.xis.context.XISComponent;

@XISComponent
@RequiredArgsConstructor
class RestClientFactoryImpl implements RestClientFactory {

    private final HttpClientFactory httpClientFactory;
    private final Gson gson;

    @Override
    public RestClient createRestClient(String serverUrl) throws HttpClientException {
        try {
            return new RestClientImpl(httpClientFactory.createHttpClient(serverUrl), gson);
        } catch (Exception e) {
            throw new HttpClientException("Failed to create RestClient for " + serverUrl, e);
        }
    }
}
