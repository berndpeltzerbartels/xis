package one.xis.spring;

import lombok.Setter;
import one.xis.server.FrontendService;
import org.springframework.stereotype.Component;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
class SpringFilter extends HttpFilter {

    @Setter
    private FrontendService frontendService;

    @Override
    protected void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
        if (request.getRequestURI().equals("/") || request.getRequestURI().equals("") || request.getRequestURI().endsWith(".html")) {
            response.setContentType("text/html");
            try (var writer = response.getWriter()) {
                writer.println(frontendService.getRootPageHtml());
            }
        } else {
            super.doFilter(request, response, chain);
        }
    }
}
