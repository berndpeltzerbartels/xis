package one.xis.http;

import java.util.concurrent.CompletionStage;

/**
 * Open server-sent-events connection for one browser client.
 */
public interface SseEmitter {

    /**
     * Sends one SSE payload and completes when the transport has accepted or rejected the write.
     */
    CompletionStage<Void> send(String data);

    /**
     * Closes the SSE connection.
     */
    void close();

    /**
     * @return {@code true} while the connection can still accept events
     */
    boolean isOpen();
}
