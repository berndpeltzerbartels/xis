package one.xis.spring;

import jakarta.servlet.AsyncContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import one.xis.http.HttpRequest;
import one.xis.http.HttpResponse;
import one.xis.http.SseEndpoint;
import one.xis.server.SseService;
import org.springframework.stereotype.Component;
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

        AsyncContext asyncContext = servletRequest.startAsync();
        asyncContext.setTimeout(0); // no timeout

        SpringSseEmitter emitter = new SpringSseEmitter(asyncContext);
        sseService.registerEmitter(clientId, userId, emitter);
        asyncContext.addListener(new jakarta.servlet.AsyncListener() {
            @Override
            public void onComplete(jakarta.servlet.AsyncEvent e) {
                sseService.unregisterEmitter(clientId, emitter);
            }

            @Override
            public void onTimeout(jakarta.servlet.AsyncEvent e) {
                sseService.unregisterEmitter(clientId, emitter);
            }

            @Override
            public void onError(jakarta.servlet.AsyncEvent e) {
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
