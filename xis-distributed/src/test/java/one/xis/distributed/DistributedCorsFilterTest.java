package one.xis.distributed;

import one.xis.http.FilterChain;
import one.xis.http.HttpMethod;
import one.xis.http.HttpRequest;
import one.xis.http.HttpResponse;
import one.xis.server.LocalUrlHolder;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DistributedCorsFilterTest {

    private final XisDistributedConfig config = new TestConfig();
    private final LocalUrlHolder localUrlHolder = mock(LocalUrlHolder.class);
    private final DistributedCorsFilter filter = new DistributedCorsFilter(config, localUrlHolder);

    @Test
    void allowsConfiguredDistributedOrigins() {
        var request = mock(HttpRequest.class);
        var response = mock(HttpResponse.class);
        var chain = mock(FilterChain.class);

        when(localUrlHolder.localUrlIsSet()).thenReturn(true);
        when(localUrlHolder.getUrl()).thenReturn("http://remote.example.test");
        when(request.getHeader("Origin")).thenReturn("http://shell.example.test");
        when(request.getHttpMethod()).thenReturn(HttpMethod.GET);

        filter.doFilter(request, response, chain);

        verify(response).addHeader("Access-Control-Allow-Origin", "http://shell.example.test");
        verify(response).addHeader("Access-Control-Allow-Credentials", "true");
        verify(chain).doFilter(request, response);
    }

    @Test
    void rejectsOriginsOutsideDistributedTopology() {
        var request = mock(HttpRequest.class);
        var response = mock(HttpResponse.class);
        var chain = mock(FilterChain.class);

        when(localUrlHolder.localUrlIsSet()).thenReturn(true);
        when(localUrlHolder.getUrl()).thenReturn("http://remote.example.test");
        when(request.getHeader("Origin")).thenReturn("http://evil.example.test");
        when(request.getHttpMethod()).thenReturn(HttpMethod.GET);

        filter.doFilter(request, response, chain);

        verify(response).setStatusCode(403);
        verify(chain, never()).doFilter(request, response);
    }

    @Test
    void handlesPreflightForConfiguredOrigins() {
        var request = mock(HttpRequest.class);
        var response = mock(HttpResponse.class);
        var chain = mock(FilterChain.class);

        when(localUrlHolder.localUrlIsSet()).thenReturn(false);
        when(request.getHeader("Origin")).thenReturn("http://shell.example.test");
        when(request.getHttpMethod()).thenReturn(HttpMethod.OPTIONS);

        filter.doFilter(request, response, chain);

        verify(response).addHeader("Access-Control-Allow-Origin", "http://shell.example.test");
        verify(response).setStatusCode(204);
        verify(chain, never()).doFilter(request, response);
    }

    @Test
    void usesAllowedOriginsCopiedAtStartup() {
        var mutableConfig = new MutableConfig();
        mutableConfig.hosts.add("http://shell.example.test");
        var cachedFilter = new DistributedCorsFilter(mutableConfig, localUrlHolder);
        mutableConfig.hosts.clear();
        var request = mock(HttpRequest.class);
        var response = mock(HttpResponse.class);
        var chain = mock(FilterChain.class);

        when(localUrlHolder.localUrlIsSet()).thenReturn(false);
        when(request.getHeader("Origin")).thenReturn("http://shell.example.test");
        when(request.getHttpMethod()).thenReturn(HttpMethod.GET);

        cachedFilter.doFilter(request, response, chain);

        verify(response).addHeader("Access-Control-Allow-Origin", "http://shell.example.test");
        verify(chain).doFilter(request, response);
    }

    private static class MutableConfig implements XisDistributedConfig {
        private final List<String> hosts = new ArrayList<>();

        @Override
        public List<String> getHosts() {
            return hosts;
        }
    }

    private static class TestConfig implements XisDistributedConfig {

        @Override
        public List<String> getHosts() {
            return List.of("http://shell.example.test");
        }
    }
}
