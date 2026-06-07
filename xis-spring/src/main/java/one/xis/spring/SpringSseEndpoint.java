package one.xis.spring;

import jakarta.servlet.AsyncContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import one.xis.context.Component;
import one.xis.http.HttpRequest;
import one.xis.http.HttpResponse;
import one.xis.http.SseEmitter;
import one.xis.http.SseEndpoint;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.net.URI;
import java.util.function.Consumer;

@Slf4j
@Component
class SpringSseEndpoint implements SseEndpoint {

    @Override
    public void open(HttpRequest request, HttpResponse response, Consumer<SseEmitter> onOpen, Consumer<SseEmitter> onClose) {
        ServletRequestAttributes attrs =
                (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
        HttpServletRequest servletRequest = attrs.getRequest();
        HttpServletResponse servletResponse = attrs.getResponse();
        if (servletResponse == null) {
            throw new IllegalStateException("SpringSseEndpoint: no HttpServletResponse available");
        }

        servletResponse.setStatus(200);
        servletResponse.setContentType("text/event-stream; charset=UTF-8");
        servletResponse.setHeader("Cache-Control", "no-cache");
        servletResponse.setHeader("Connection", "keep-alive");
        servletResponse.setHeader("X-Accel-Buffering", "no");
        String origin = request.getHeader("Origin");
        if (origin != null && !origin.isBlank()) {
            if (!isSameOriginRequest(request, origin)) {
                servletResponse.setStatus(403);
                response.setStatusCode(403);
                return;
            }
            servletResponse.setHeader("Access-Control-Allow-Origin", origin);
            servletResponse.setHeader("Access-Control-Allow-Credentials", "true");
            servletResponse.addHeader("Vary", "Origin");
        }

        AsyncContext asyncContext = servletRequest.startAsync();
        asyncContext.setTimeout(0); // no timeout

        SpringSseEmitter emitter = new SpringSseEmitter(asyncContext);
        onOpen.accept(emitter);
        asyncContext.addListener(new jakarta.servlet.AsyncListener() {
            @Override
            public void onComplete(jakarta.servlet.AsyncEvent e) {
                onClose.accept(emitter);
            }

            @Override
            public void onTimeout(jakarta.servlet.AsyncEvent e) {
                emitter.close();
                onClose.accept(emitter);
            }

            @Override
            public void onError(jakarta.servlet.AsyncEvent e) {
                if (SpringSseEmitter.isClientDisconnect(e.getThrowable())) {
                    log.debug("open: SSE connection closed by client");
                } else {
                    log.error("open: SSE connection failed", e.getThrowable());
                }
                emitter.close();
                onClose.accept(emitter);
            }

            @Override
            public void onStartAsync(jakarta.servlet.AsyncEvent e) {
            }
        });

        log.debug("open: SSE connection opened");
        response.setStatusCode(200);
    }

    private boolean isSameOriginRequest(HttpRequest request, String origin) {
        try {
            URI originUri = URI.create(origin);
            String host = forwardedOrHostHeader(request);
            if (host == null || host.isBlank() || originUri.getScheme() == null || originUri.getHost() == null) {
                return false;
            }
            URI requestUri = URI.create(requestScheme(request) + "://" + host);
            return normalizedOrigin(originUri).equals(normalizedOrigin(requestUri));
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private String requestScheme(HttpRequest request) {
        String forwardedProto = request.getHeader("X-Forwarded-Proto");
        if (forwardedProto != null && !forwardedProto.isBlank()) {
            return firstForwardedValue(forwardedProto).toLowerCase();
        }
        return request.isSecure() ? "https" : "http";
    }

    private String forwardedOrHostHeader(HttpRequest request) {
        String forwardedHost = request.getHeader("X-Forwarded-Host");
        if (forwardedHost != null && !forwardedHost.isBlank()) {
            return firstForwardedValue(forwardedHost);
        }
        return request.getHeader("Host");
    }

    private String firstForwardedValue(String value) {
        int comma = value.indexOf(',');
        return (comma >= 0 ? value.substring(0, comma) : value).trim();
    }

    private String normalizedOrigin(URI uri) {
        String scheme = uri.getScheme().toLowerCase();
        String host = uri.getHost().toLowerCase();
        int port = uri.getPort();
        if (port == defaultPort(scheme)) {
            port = -1;
        }
        return scheme + "://" + host + (port >= 0 ? ":" + port : "");
    }

    private int defaultPort(String scheme) {
        if ("http".equals(scheme)) {
            return 80;
        }
        if ("https".equals(scheme)) {
            return 443;
        }
        return -1;
    }
}
