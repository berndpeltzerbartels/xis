package one.xis.http;


import one.xis.context.XISComponent;

@XISComponent
class HttpClientFactoryImpl implements HttpClientFactory {
    @Override
    public HttpClient createHttpClient(String serverUrl) {
        return new HttpClientImpl(serverUrl);
    }
}
