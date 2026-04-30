package one.xis.spring;

import jakarta.servlet.AsyncContext;
import lombok.extern.slf4j.Slf4j;
import one.xis.http.SseEmitter;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
class SpringSseEmitter implements SseEmitter {

    private final AsyncContext asyncContext;
    private final AtomicBoolean open = new AtomicBoolean(true);

    SpringSseEmitter(AsyncContext asyncContext) {
        this.asyncContext = asyncContext;
    }

    @Override
    public void send(String data) {
        if (!isOpen()) {
            log.warn("send: emitter is closed - message dropped");
            return;
        }
        try {
            PrintWriter writer = asyncContext.getResponse().getWriter();
            writer.print(data);
            writer.flush();
            if (writer.checkError()) {
                log.warn("send: write error - marking emitter as closed");
                open.set(false);
            }
        } catch (IOException e) {
            log.warn("send: IOException - {}", e.getMessage());
            open.set(false);
        }
    }

    @Override
    public void close() {
        if (open.compareAndSet(true, false)) {
            asyncContext.complete();
        }
    }

    @Override
    public boolean isOpen() {
        return open.get();
    }
}
