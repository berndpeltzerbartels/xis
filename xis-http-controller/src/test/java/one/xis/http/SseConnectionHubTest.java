package one.xis.http;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import static org.assertj.core.api.Assertions.assertThat;

class SseConnectionHubTest {

    @Test
    void sendsDataToRegisteredConnectionKey() {
        var hub = new SseConnectionHub();
        var emitter = new RecordingEmitter();
        var key = SseConnectionKey.of("game-player", "player-1");

        hub.register(key, emitter);
        hub.sendData(key, "{\"type\":\"ping\"}").toCompletableFuture().join();

        assertThat(emitter.sent).containsExactly("data:{\"type\":\"ping\"}\n\n");
    }

    @Test
    void sameEmitterCanBeRegisteredUnderSeveralKeys() {
        var hub = new SseConnectionHub();
        var emitter = new RecordingEmitter();

        hub.register("game-player", "player-1", emitter);
        hub.register("game-match", "match-7", emitter);

        hub.sendData("game-match", "match-7", "state").toCompletableFuture().join();
        hub.sendData("game-player", "player-1", "private").toCompletableFuture().join();

        assertThat(emitter.sent).containsExactly("data:state\n\n", "data:private\n\n");
    }

    @Test
    void failedSendRemovesEmitterFromAllKeys() {
        var hub = new SseConnectionHub();
        var emitter = new RecordingEmitter();
        emitter.failSend = true;
        var key = SseConnectionKey.of("game-player", "player-1");
        var matchKey = SseConnectionKey.of("game-match", "match-7");

        hub.register(key, emitter);
        hub.register(matchKey, emitter);
        try {
            hub.sendData(key, "state").toCompletableFuture().join();
        } catch (RuntimeException ignored) {
        }

        assertThat(hub.connectionCount(key)).isZero();
        assertThat(hub.connectionCount(matchKey)).isZero();
        assertThat(emitter.closed).isTrue();
    }

    private static class RecordingEmitter implements SseEmitter {
        final List<String> sent = new ArrayList<>();
        boolean open = true;
        boolean closed;
        boolean failSend;

        @Override
        public CompletionStage<Void> send(String data) {
            if (failSend) {
                return CompletableFuture.failedFuture(new SseSendFailedException("failed"));
            }
            sent.add(data);
            return CompletableFuture.completedFuture(null);
        }

        @Override
        public void close() {
            closed = true;
            open = false;
        }

        @Override
        public boolean isOpen() {
            return open;
        }
    }
}
