package one.xis.spring;

import jakarta.servlet.AsyncContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import one.xis.http.HttpRequest;
import one.xis.http.HttpResponse;
import one.xis.server.SseService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Map;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class SpringSseEndpointTest {

    @AfterEach
    void resetRequestContext() {
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    void rejectsCrossOriginSseRequestBeforeOpeningAsyncContext() {
        var servletRequest = mock(HttpServletRequest.class);
        var servletResponse = mock(HttpServletResponse.class);
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(servletRequest, servletResponse));

        var request = mock(HttpRequest.class);
        var response = mock(HttpResponse.class);
        when(request.getHeader("Origin")).thenReturn("https://evil.example");
        when(request.getHeader("Host")).thenReturn("app.example");
        when(request.isSecure()).thenReturn(true);

        var sseService = mock(SseService.class);

        new SpringSseEndpoint(sseService).open("client-1", "user-1", request, response);

        verify(servletResponse).setStatus(403);
        verify(response).setStatusCode(403);
        verify(servletRequest, never()).startAsync();
        verifyNoInteractions(sseService);
    }

    @Test
    void acceptsForwardedSameOriginSseRequest() {
        var servletRequest = mock(HttpServletRequest.class);
        var servletResponse = mock(HttpServletResponse.class);
        var asyncContext = mock(AsyncContext.class);
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(servletRequest, servletResponse));
        when(servletRequest.startAsync()).thenReturn(asyncContext);
        when(asyncContext.getResponse()).thenReturn(servletResponse);
        try {
            when(servletResponse.getWriter()).thenReturn(new PrintWriter(new StringWriter()));
        } catch (java.io.IOException e) {
            throw new RuntimeException(e);
        }

        var request = mock(HttpRequest.class);
        var response = mock(HttpResponse.class);
        Map<String, String> headers = Map.of(
                "Origin", "https://app.example",
                "Host", "internal:8080",
                "X-Forwarded-Proto", "https",
                "X-Forwarded-Host", "app.example"
        );
        when(request.getHeader(org.mockito.ArgumentMatchers.anyString()))
                .thenAnswer(invocation -> headers.getOrDefault(invocation.getArgument(0), ""));

        var sseService = mock(SseService.class);

        new SpringSseEndpoint(sseService).open("client-1", "user-1", request, response);

        verify(servletRequest).startAsync();
        verify(servletResponse).setHeader("Access-Control-Allow-Origin", "https://app.example");
        verify(sseService).registerEmitter(org.mockito.ArgumentMatchers.eq("client-1"),
                org.mockito.ArgumentMatchers.eq("user-1"), org.mockito.ArgumentMatchers.any());
    }
}
