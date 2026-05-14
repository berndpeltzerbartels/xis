package one.xis.http;

import java.util.concurrent.CompletionStage;

public interface SseEmitter {

    /**
     * Sends one SSE payload and completes when the transport has accepted or rejected the write.
     */
    CompletionStage<Void> send(String data);

    void close();

    boolean isOpen();
}
