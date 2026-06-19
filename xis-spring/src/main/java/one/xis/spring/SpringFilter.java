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
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.multipart.support.StandardServletMultipartResolver;
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
    private final StandardServletMultipartResolver multipartResolver = new StandardServletMultipartResolver();

    @Override
    protected void doFilter(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, FilterChain chain) throws IOException, ServletException {
        MultipartHttpServletRequest multipartRequest = null;
        HttpServletRequest request = httpServletRequest;
        if (multipartResolver.isMultipart(httpServletRequest)) {
            multipartRequest = multipartResolver.resolveMultipart(httpServletRequest);
            request = multipartRequest;
        }
        try {
            doXisFilter(request, httpServletResponse, chain);
        } finally {
            if (multipartRequest != null) {
                multipartResolver.cleanupMultipart(multipartRequest);
            }
        }
    }

    private void doXisFilter(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, FilterChain chain) throws IOException, ServletException {
        if (!isInitialized()) {
            httpServletResponse.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
            return;
        }
        if (!localUrlHolder.localUrlIsSet()) {
            String baseUrl = ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString();
            localUrlHolder.setLocalUrl(baseUrl);
        }
        if (httpServletRequest.getRequestURI().startsWith("/public/")) {
            chain.doFilter(httpServletRequest, httpServletResponse);
            return;
        }
        var request = new SpringHttpRequest(httpServletRequest);
        var response = new HttpResponseImpl(request.isSecure());
        restControllerService.doInvocation(request, response);
        if (response.getStatusCode() != null && response.getStatusCode() == 404) {
            if (isFrontendRequest(httpServletRequest)) {
                writeFrontendShell(httpServletResponse);
                return;
            }
            chain.doFilter(httpServletRequest, httpServletResponse);
        } else {
            commit(response, httpServletResponse);
        }

    }

    private boolean isInitialized() {
        return frontendService != null
                && restControllerService != null
                && localUrlHolder != null;
    }


    private boolean isFrontendRequest(HttpServletRequest request) {
        String path = request.getRequestURI();
        return (path.equals("/") || path.isEmpty() || path.endsWith(".html")) && !path.startsWith("/xis");
    }

    private void writeFrontendShell(HttpServletResponse response) throws IOException {
        response.setContentType("text/html");
        try (var writer = response.getWriter()) {
            writer.println(frontendService.getRootPageHtml());
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
        private final boolean secureRequest;

        HttpResponseImpl(boolean secureRequest) {
            this.secureRequest = secureRequest;
        }

        @Override
        public void setStatusCode(int i) {
            this.statusCode = i;
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

        /**
         * Adds an HTTP-only token cookie and only marks it {@code Secure} when
         * the original request is HTTPS. Safari drops Secure cookies on
         * {@code http://localhost}, so local login would otherwise succeed on
         * the server and immediately disappear in the browser. HTTPS requests,
         * including production traffic behind a proxy that reports HTTPS
         * correctly, still receive {@code Secure} token cookies.
         */
        @Override
        public void addSecureCookie(String name, String value, Duration maxAge) {
            String secureAttribute = secureRequest ? "; Secure" : "";
            String cookie = String.format("%s=%s; Max-Age=%d; Path=/%s; HttpOnly; SameSite=Lax",
                    name, value, maxAge.getSeconds(), secureAttribute);
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
