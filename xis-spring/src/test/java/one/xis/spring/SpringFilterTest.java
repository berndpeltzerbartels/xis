package one.xis.spring;

import jakarta.servlet.FilterChain;
import one.xis.http.ContentType;
import one.xis.http.RestControllerService;
import one.xis.server.FrontendService;
import one.xis.server.LocalUrlHolder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SpringFilterTest {

    private final FrontendService frontendService = mock(FrontendService.class);
    private final RestControllerService restControllerService = mock(RestControllerService.class);
    private final LocalUrlHolder localUrlHolder = mock(LocalUrlHolder.class);
    private final FilterChain chain = mock(FilterChain.class);
    private final SpringFilter filter = new SpringFilter();

    @BeforeEach
    void setUp() {
        filter.setFrontendService(frontendService);
        filter.setRestControllerService(restControllerService);
        filter.setLocalUrlHolder(localUrlHolder);
        when(localUrlHolder.localUrlIsSet()).thenReturn(true);
    }

    @Test
    void htmlControllerResponseWinsBeforeFrontendShellFallback() throws Exception {
        var request = new MockHttpServletRequest("GET", "/counter.html");
        var response = new MockHttpServletResponse();
        when(frontendService.getRootPageHtml()).thenReturn("<html>shell</html>");

        org.mockito.Mockito.doAnswer(invocation -> {
            SpringFilter.HttpResponseImpl httpResponse = invocation.getArgument(1);
            httpResponse.setStatusCode(200);
            httpResponse.setContentType(ContentType.TEXT_HTML_UTF8);
            httpResponse.setBody("<html>controller</html>".getBytes(StandardCharsets.UTF_8));
            return null;
        }).when(restControllerService).doInvocation(any(), any());

        filter.doFilter(request, response, chain);

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.getContentAsString()).contains("controller");
        assertThat(response.getContentAsString()).doesNotContain("shell");
        verify(chain, never()).doFilter(any(), any());
    }

    @Test
    void htmlRequestFallsBackToFrontendShellAfterControllerNotFound() throws Exception {
        var request = new MockHttpServletRequest("GET", "/deep-link.html");
        var response = new MockHttpServletResponse();
        when(frontendService.getRootPageHtml()).thenReturn("<html>shell</html>");

        org.mockito.Mockito.doAnswer(invocation -> {
            SpringFilter.HttpResponseImpl httpResponse = invocation.getArgument(1);
            httpResponse.setStatusCode(404);
            return null;
        }).when(restControllerService).doInvocation(any(), any());

        filter.doFilter(request, response, chain);

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.getContentAsString()).contains("shell");
        verify(chain, never()).doFilter(any(), any());
    }
}
