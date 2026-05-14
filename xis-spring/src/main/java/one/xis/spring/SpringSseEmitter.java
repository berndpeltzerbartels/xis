package one.xis.spring;

import jakarta.servlet.AsyncContext;
import lombok.extern.slf4j.Slf4j;
import one.xis.http.SseEmitter;
import one.xis.http.SseSendFailedException;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
class SpringSseEmitter implements SseEmitter {

    private final AsyncContext asyncContext;
    private final AtomicBoolean open = new AtomicBoolean(true);

    SpringSseEmitter(AsyncContext asyncContext) {
        this.asyncContext = asyncContext;
    }

    @Override
    public CompletionStage<Void> send(String data) {
        if (!isOpen()) {
            return CompletableFuture.failedFuture(new SseSendFailedException("SSE emitter is closed"));
        }
        try {
            PrintWriter writer = asyncContext.getResponse().getWriter();
            writer.print(data);
            writer.flush();
            if (writer.checkError()) {
                log.debug("send: client closed SSE connection");
                close();
                return CompletableFuture.failedFuture(new SseSendFailedException("SSE write failed: client closed connection"));
            }
            return CompletableFuture.completedFuture(null);
        } catch (IOException e) {
            if (isClientDisconnect(e)) {
                log.debug("send: client closed SSE connection - {}", e.getMessage());
            } else {
                log.error("send: SSE write failed", e);
            }
            close();
            return CompletableFuture.failedFuture(new SseSendFailedException("SSE write failed", e));
        }
    }

    @Override
    public void close() {
        if (open.compareAndSet(true, false)) {
            try {
                asyncContext.complete();
            } catch (IllegalStateException ignored) {
                // The servlet container may already have completed the async request.
            }
        }
    }

    @Override
    public boolean isOpen() {
        return open.get();
    }

    static boolean isClientDisconnect(Throwable throwable) {
        Throwable current = throwable;
        while (current != null) {
            String className = current.getClass().getName();
            String message = current.getMessage();
            if (className.contains("ClientAbortException")
                    || className.contains("EofException")
                    || containsIgnoreCase(message, "broken pipe")
                    || containsIgnoreCase(message, "connection reset")
                    || containsIgnoreCase(message, "stream closed")
                    || containsIgnoreCase(message, "socket closed")) {
                return true;
            }
            current = current.getCause();
        }
        return false;
    }

    private static boolean containsIgnoreCase(String value, String expected) {
        return value != null && value.toLowerCase(Locale.ROOT).contains(expected);
    }
}
