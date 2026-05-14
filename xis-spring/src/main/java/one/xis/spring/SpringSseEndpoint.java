package one.xis.spring;

import jakarta.servlet.AsyncContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import one.xis.context.Component;
import one.xis.http.HttpRequest;
import one.xis.http.HttpResponse;
import one.xis.http.SseEndpoint;
import one.xis.server.SseService;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Slf4j
@Component
@RequiredArgsConstructor
class SpringSseEndpoint implements SseEndpoint {

    private final SseService sseService;

    @Override
    public void open(String clientId, String userId, HttpRequest request, HttpResponse response) {
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
            servletResponse.setHeader("Access-Control-Allow-Origin", origin);
            servletResponse.setHeader("Access-Control-Allow-Credentials", "true");
            servletResponse.addHeader("Vary", "Origin");
        }

        AsyncContext asyncContext = servletRequest.startAsync();
        asyncContext.setTimeout(0); // no timeout

        SpringSseEmitter emitter = new SpringSseEmitter(asyncContext);
        sseService.registerEmitter(clientId, userId, emitter);
        emitter.send(": connected\n\n").whenComplete((ignored, throwable) -> {
            if (throwable != null) {
                sseService.unregisterEmitter(clientId, emitter);
            }
        });
        asyncContext.addListener(new jakarta.servlet.AsyncListener() {
            @Override
            public void onComplete(jakarta.servlet.AsyncEvent e) {
                sseService.unregisterEmitter(clientId, emitter);
            }

            @Override
            public void onTimeout(jakarta.servlet.AsyncEvent e) {
                emitter.close();
                sseService.unregisterEmitter(clientId, emitter);
            }

            @Override
            public void onError(jakarta.servlet.AsyncEvent e) {
                if (SpringSseEmitter.isClientDisconnect(e.getThrowable())) {
                    log.debug("open: SSE connection closed by client for clientId={}", clientId);
                } else {
                    log.error("open: SSE connection failed for clientId={}", clientId, e.getThrowable());
                }
                emitter.close();
                sseService.unregisterEmitter(clientId, emitter);
            }

            @Override
            public void onStartAsync(jakarta.servlet.AsyncEvent e) {
            }
        });

        log.debug("open: SSE connection opened for clientId={}", clientId);
        response.setStatusCode(200);
    }
}
