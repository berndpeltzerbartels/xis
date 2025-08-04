package one.xis.spring;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Setter;
import one.xis.http.RestControllerService;
import one.xis.server.FrontendService;
import one.xis.server.LocalUrlHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.IOException;
import java.util.Set;

@Setter
@Component
class SpringFilter extends HttpFilter {

    private FrontendService frontendService;
    private RestControllerService restControllerService;
    private LocalUrlHolder localUrlHolder;
    private static final Set<String> SPRING_PUBLIC_RESOURCE_FILES = Set.of(
            "/favicon.ico", "/favicon.png", "/favicon.svg", "/robots.txt"
    );

    @Override
    protected void doFilter(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, FilterChain chain) throws IOException, ServletException {
        if (!localUrlHolder.localUrlIsSet()) {
            String baseUrl = ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString();
            localUrlHolder.setLocalUrl(baseUrl);
        }
        if (httpServletRequest.getRequestURI().equals("/") || httpServletRequest.getRequestURI().isEmpty() || httpServletRequest.getRequestURI().endsWith(".html")) {
            httpServletResponse.setContentType("text/html");
            try (var writer = httpServletResponse.getWriter()) {
                writer.println(frontendService.getRootPageHtml());
            }
            return;
        }
        var request = new SpringHttpRequest(httpServletRequest);
        var response = new SpringHttpResponse(httpServletResponse);
        restControllerService.doInvocation(request, response);
        if (response.getStatusCode() != null & response.getStatusCode() == 404) {
            chain.doFilter(httpServletRequest, httpServletResponse);
        }
    }
}
