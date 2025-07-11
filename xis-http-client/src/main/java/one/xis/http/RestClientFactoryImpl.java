package one.xis.http;

import com.google.gson.Gson;
import lombok.RequiredArgsConstructor;
import one.xis.context.XISComponent;
import one.xis.server.UrlHolder;

@XISComponent
@RequiredArgsConstructor
class RestClientFactoryImpl implements RestClientFactory {

    private final HttpClientFactory httpClientFactory;
    private final Gson gson;

    @Override
    public RestClient createRestClient(UrlHolder serverUrl) throws HttpClientException {
        try {
            return new RestClientImpl(httpClientFactory.createHttpClient(serverUrl), gson);
        } catch (Exception e) {
            throw new HttpClientException("Failed to create RestClient for " + serverUrl, e);
        }
    }
}
