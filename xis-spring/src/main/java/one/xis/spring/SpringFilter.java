package one.xis.spring;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Getter;
import lombok.Setter;
import one.xis.http.ContentType;
import one.xis.http.HttpResponse;
import one.xis.http.RestControllerService;
import one.xis.server.FrontendService;
import one.xis.server.LocalUrlHolder;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.IOException;
import java.time.Duration;

@Setter
@Component
@Order(0)
class SpringFilter extends HttpFilter {

    private FrontendService frontendService;
    private RestControllerService restControllerService;
    private LocalUrlHolder localUrlHolder;

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
        if (httpServletRequest.getRequestURI().startsWith("/public/")) {
            chain.doFilter(httpServletRequest, httpServletResponse);
            return;
        }
        var request = new SpringHttpRequest(httpServletRequest);
        var response = new HttpResponseImpl();
        restControllerService.doInvocation(request, response);
        if (response.getStatusCode() != null & response.getStatusCode() == 404) {
            chain.doFilter(httpServletRequest, httpServletResponse);
        } else {
            commit(response, httpServletResponse);
        }

    }

    private void commit(HttpResponseImpl response, HttpServletResponse httpServletResponse) throws IOException {
        if (response.getRedirectLocation() != null) {
            httpServletResponse.sendRedirect(response.getRedirectLocation());
            return;
        }

        if (response.getStatusCode() != null) {
            httpServletResponse.setStatus(response.getStatusCode());
        }

        if (response.getContentType() != null) {
            httpServletResponse.setContentType(response.getContentType().getValue());
        }

        response.getHeaders().forEach((name, values) ->
                values.forEach(value -> httpServletResponse.addHeader(name, value))
        );

        byte[] body = response.getBody();
        if (body != null) {
            if (response.getContentLength() != null) {
                httpServletResponse.setContentLength(response.getContentLength());
            } else {
                httpServletResponse.setContentLength(body.length);
            }
            httpServletResponse.getOutputStream().write(body);
        }
    }

    @Getter
    class HttpResponseImpl implements HttpResponse {

        private Integer statusCode;
        private byte[] body;
        private ContentType contentType;
        private Integer contentLength;
        private final java.util.Map<String, java.util.List<String>> headers = new java.util.HashMap<>();
        private String redirectLocation;

        @Override
        public void setStatusCode(int i) {
            this.statusCode = i;
        }

        @Override
        public void setBody(String s) {
            if (s == null) {
                this.body = null;
                return;
            }
            this.body = s.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        }

        @Override
        public void setBody(byte[] body) {
            this.body = body;
        }

        @Override
        public void setContentType(ContentType contentType) {
            this.contentType = contentType;
        }

        @Override
        public void setContentLength(int contentLength) {
            this.contentLength = contentLength;
        }

        @Override
        public Integer getStatusCode() {
            return statusCode;
        }

        @Override
        public ContentType getContentType() {
            return contentType;
        }

        @Override
        public void addHeader(String name, String value) {
            this.headers.computeIfAbsent(name, k -> new java.util.ArrayList<>()).add(value);
        }

        @Override
        public void addSecureCookie(String name, String value, Duration maxAge) {
            // Baut einen einfachen Set-Cookie-Header-String
            String cookie = String.format("%s=%s; Max-Age=%d; Path=/; Secure; HttpOnly; SameSite=Lax",
                    name, value, maxAge.getSeconds());
            addHeader("Set-Cookie", cookie);
        }

        @Override
        public void sendRedirect(String location) {
            this.redirectLocation = location;
            setStatusCode(302); // Found
            addHeader("Location", location);
        }
    }

}