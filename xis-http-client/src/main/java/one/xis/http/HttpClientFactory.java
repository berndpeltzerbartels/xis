package one.xis.http;

import one.xis.server.UrlHolder;

public interface HttpClientFactory {

    HttpClient createHttpClient(UrlHolder serverUrl);
}
