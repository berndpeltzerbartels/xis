package one.xis.http;

import one.xis.context.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Generic SSE connection registry for standalone HTTP-controller use.
 * <p>
 * It deliberately does not know about XIS pages, frontlets, refresh events, or
 * authentication. Applications choose their own {@link SseConnectionKey}s and
 * can register the same emitter under several keys when needed.
 */
@Component
public class SseConnectionHub {

    private final ConcurrentHashMap<SseConnectionKey, Set<SseEmitter>> emittersByKey = new ConcurrentHashMap<>();

    public void register(SseConnectionKey key, SseEmitter emitter) {
        requireEmitter(emitter);
        emittersByKey.computeIfAbsent(key, ignored -> ConcurrentHashMap.newKeySet()).add(emitter);
    }

    public void register(String scope, String id, SseEmitter emitter) {
        register(SseConnectionKey.of(scope, id), emitter);
    }

    public void unregister(SseConnectionKey key, SseEmitter emitter) {
        Set<SseEmitter> emitters = emittersByKey.get(key);
        if (emitters == null) {
            return;
        }
        emitters.remove(emitter);
        if (emitters.isEmpty()) {
            emittersByKey.remove(key, emitters);
        }
    }

    public void unregister(String scope, String id, SseEmitter emitter) {
        unregister(SseConnectionKey.of(scope, id), emitter);
    }

    public void unregister(SseEmitter emitter) {
        emittersByKey.forEach((key, emitters) -> {
            emitters.remove(emitter);
            if (emitters.isEmpty()) {
                emittersByKey.remove(key, emitters);
            }
        });
    }

    public CompletionStage<Void> send(SseConnectionKey key, String ssePayload) {
        Set<SseEmitter> emitters = emittersByKey.get(key);
        if (emitters == null || emitters.isEmpty()) {
            return CompletableFuture.completedFuture(null);
        }
        return sendToEmitters(key, new ArrayList<>(emitters), ssePayload);
    }

    public CompletionStage<Void> send(String scope, String id, String ssePayload) {
        return send(SseConnectionKey.of(scope, id), ssePayload);
    }

    public CompletionStage<Void> sendData(SseConnectionKey key, String data) {
        return send(key, formatData(data));
    }

    public CompletionStage<Void> sendData(String scope, String id, String data) {
        return sendData(SseConnectionKey.of(scope, id), data);
    }

    public CompletionStage<Void> broadcast(String ssePayload) {
        Collection<CompletionStage<Void>> sends = emittersByKey.entrySet().stream()
                .map(entry -> sendToEmitters(entry.getKey(), new ArrayList<>(entry.getValue()), ssePayload))
                .toList();
        return allOf(sends);
    }

    public CompletionStage<Void> broadcastData(String data) {
        return broadcast(formatData(data));
    }

    public int connectionCount(SseConnectionKey key) {
        Set<SseEmitter> emitters = emittersByKey.get(key);
        return emitters == null ? 0 : emitters.size();
    }

    private CompletionStage<Void> sendToEmitters(SseConnectionKey key, Collection<SseEmitter> emitters, String payload) {
        Collection<CompletionStage<Void>> sends = emitters.stream()
                .filter(this::isOpen)
                .map(emitter -> sendToEmitter(key, emitter, payload))
                .toList();
        return allOf(sends);
    }

    private CompletionStage<Void> sendToEmitter(SseConnectionKey key, SseEmitter emitter, String payload) {
        CompletionStage<Void> sendResult;
        try {
            sendResult = emitter.send(payload);
        } catch (RuntimeException e) {
            sendResult = CompletableFuture.failedFuture(e);
        }
        if (sendResult == null) {
            sendResult = CompletableFuture.failedFuture(new IllegalStateException("SseEmitter.send returned null"));
        }
        return sendResult.whenComplete((ignored, throwable) -> {
            if (throwable != null) {
                unregister(emitter);
                closeQuietly(emitter);
            }
        });
    }

    private CompletionStage<Void> allOf(Collection<CompletionStage<Void>> stages) {
        CompletableFuture<?>[] futures = stages.stream()
                .map(CompletionStage::toCompletableFuture)
                .toArray(CompletableFuture[]::new);
        return CompletableFuture.allOf(futures).exceptionally(throwable -> {
            throw new CompletionException(throwable);
        });
    }

    private boolean isOpen(SseEmitter emitter) {
        return emitter != null && emitter.isOpen();
    }

    private void requireEmitter(SseEmitter emitter) {
        if (emitter == null) {
            throw new IllegalArgumentException("SSE emitter must not be null");
        }
    }

    private void closeQuietly(SseEmitter emitter) {
        try {
            emitter.close();
        } catch (RuntimeException ignored) {
            // The connection is already unusable; callers observe the failed send.
        }
    }

    private String formatData(String data) {
        return "data:" + (data == null ? "" : data) + "\n\n";
    }
}
