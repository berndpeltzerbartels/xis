package one.xis.http;

import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.*;

class CORSFilterTest {

    private final CORSFilter filter = new CORSFilter();

    @Test
    void addsCorsHeadersForCrossOriginGetRequests() {
        var request = mock(HttpRequest.class);
        var response = mock(HttpResponse.class);
        var chain = mock(FilterChain.class);

        when(request.getHeader("Origin")).thenReturn("https://shop.example.com");
        when(request.getHttpMethod()).thenReturn(HttpMethod.GET);

        filter.doFilter(request, response, chain);

        verify(response).addHeader("Access-Control-Allow-Origin", "https://shop.example.com");
        verify(response).addHeader("Access-Control-Allow-Credentials", "true");
        verify(response).addHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        verify(response).addHeader("Access-Control-Allow-Headers", "Content-Type, Authorization");
        verify(chain).doFilter(request, response);
        verify(response, never()).setStatusCode(204);
    }

    @Test
    void handlesPreflightRequestWithoutInvokingControllerChain() {
        var request = mock(HttpRequest.class);
        var response = mock(HttpResponse.class);
        var chain = mock(FilterChain.class);

        when(request.getHeader("Origin")).thenReturn("https://shop.example.com");
        when(request.getHttpMethod()).thenReturn(HttpMethod.OPTIONS);

        filter.doFilter(request, response, chain);

        verify(response).addHeader("Access-Control-Allow-Origin", "https://shop.example.com");
        verify(response).addHeader("Access-Control-Allow-Credentials", "true");
        verify(response).addHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        verify(response).addHeader("Access-Control-Allow-Headers", "Content-Type, Authorization");
        verify(response).setStatusCode(204);
        verify(chain, never()).doFilter(request, response);
    }
}
