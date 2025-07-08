package one.xis.http;


import lombok.RequiredArgsConstructor;
import one.xis.context.XISComponent;
import one.xis.server.UrlHolder;

@XISComponent
@RequiredArgsConstructor
class HttpClientFactoryImpl implements HttpClientFactory {
    @Override
    public HttpClient createHttpClient(UrlHolder urlHolder) {
        return new HttpClientImpl(urlHolder);
    }
}
