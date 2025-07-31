package one.xis.http.client;


import lombok.RequiredArgsConstructor;
import one.xis.context.XISComponent;

@XISComponent
@RequiredArgsConstructor
class HttpClientFactoryImpl implements HttpClientFactory {
    @Override
    public HttpClient createHttpClient(String serverUrl) {
        return new HttpClientImpl(serverUrl);
    }

    @Override
    public HttpClient createHttpClient() {
        return new HttpClientImpl("");
    }
}
