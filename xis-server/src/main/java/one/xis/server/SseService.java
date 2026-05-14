package one.xis.server;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import one.xis.Frontlet;
import one.xis.Page;
import one.xis.RefreshEvent;
import one.xis.RefreshEventPublisher;
import one.xis.RefreshOnUpdateEvents;
import one.xis.RefreshTarget;
import one.xis.auth.token.DefaultUserSecurityService;
import one.xis.auth.token.UserSecurityService;
import one.xis.context.DefaultComponent;
import one.xis.context.Init;
import one.xis.context.Inject;
import one.xis.http.SseEmitter;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@DefaultComponent
@RequiredArgsConstructor
public class SseService implements RefreshEventPublisher {

    /**
     * clientId -> active SSE emitter
     */
    private final Map<String, SseEmitter> emitters = new ConcurrentHashMap<>();
    private final Map<String, String> userIdByClientId = new ConcurrentHashMap<>();
    private final Map<String, Set<String>> clientIdsByUserId = new ConcurrentHashMap<>();
    private final Map<String, Queue<PendingEvent>> pendingEventsByClientId = new ConcurrentHashMap<>();
    private final Map<String, Long> knownClientExpiresAt = new ConcurrentHashMap<>();
    private final UserSecurityService userSecurityService;
    private static final long PENDING_EVENT_TTL_MILLIS = ClientConfigService.PENDING_EVENT_TTL_SECONDS * 1000;

    /**
     * All valid event keys declared via @RefreshOnUpdateEvents across all controllers
     */
    private Set<String> knownEventKeys = Collections.emptySet();

    @Inject(annotatedWith = Page.class)
    private Collection<Object> pageControllers;

    @Inject(annotatedWith = Frontlet.class)
    private Collection<Object> frontletControllers;

    // -------------------------------------------------------------------------
    // Lifecycle
    // -------------------------------------------------------------------------

    @Init
    void initKnownEventKeys() {
        knownEventKeys = Stream.concat(pageControllers.stream(), frontletControllers.stream())
                .map(Object::getClass)
                .filter(c -> c.isAnnotationPresent(RefreshOnUpdateEvents.class))
                .flatMap(c -> Arrays.stream(c.getAnnotation(RefreshOnUpdateEvents.class).value()))
                .collect(Collectors.toUnmodifiableSet());
        log.debug("initKnownEventKeys: registered {} event key(s): {}", knownEventKeys.size(), knownEventKeys);
    }

    // -------------------------------------------------------------------------
    // Emitter registry
    // -------------------------------------------------------------------------

    public void registerEmitter(String clientId, String userId, SseEmitter emitter) {
        SseEmitter old = emitters.put(clientId, emitter);
        if (old != null && old.isOpen()) {
            log.debug("registerEmitter: closing stale emitter for clientId={}", clientId);
            old.close();
        }
        rememberClient(clientId);
        updateUserRegistration(clientId, userId);
        log.debug("registerEmitter: clientId={}", clientId);
        flushPendingEvents(clientId, emitter);
    }

    public void unregisterEmitter(String clientId, SseEmitter emitter) {
        if (emitters.remove(clientId, emitter)) {
            rememberClient(clientId);
            log.debug("unregisterEmitter: clientId={}", clientId);
        } else {
            log.debug("unregisterEmitter: ignoring stale emitter for clientId={}", clientId);
        }
    }

    // -------------------------------------------------------------------------
    // RefreshEventPublisher
    // -------------------------------------------------------------------------

    @Override
    public void publish(RefreshEvent refreshEvent) {
        validateEventKey(refreshEvent.getEventKey());
        String payload = buildPayload(refreshEvent.getEventKey());
        for (RefreshTarget target : refreshEvent.getTargets()) {
            dispatchToTarget(target, payload);
        }
    }

    private void dispatchToTarget(RefreshTarget target, String payload) {
        switch (target.getType()) {
            case ALL -> sendToAll(payload);
            case ALL_USERS -> sendToAllUsers(payload);
            case CLIENT -> target.getValues().forEach(clientId -> sendToClient(clientId, payload));
            case USER -> sendToUsers(target.getValues(), payload);
        }
    }

    private void sendToAll(String payload) {
        pruneKnownClients();
        Set<String> attemptedClients = new HashSet<>();
        emitters.forEach((clientId, emitter) -> {
            attemptedClients.add(clientId);
            if (emitter.isOpen()) {
                sendToEmitter(clientId, emitter, payload, () -> log.debug("sendToAll: sent event to clientId={}", clientId));
            } else {
                emitters.remove(clientId, emitter);
                rememberClient(clientId);
                queuePendingEvent(clientId, payload, "emitter is closed");
            }
        });
        knownClientExpiresAt.keySet().stream()
                .filter(clientId -> !emitters.containsKey(clientId))
                .filter(clientId -> !attemptedClients.contains(clientId))
                .forEach(clientId -> queuePendingEvent(clientId, payload, "no emitter registered"));
    }

    private void sendToUsers(Collection<String> userIds, String payload) {
        requireUserTargeting();
        pruneKnownClients();
        userIds.forEach(userId -> {
            Set<String> clientIds = clientIdsByUserId.getOrDefault(userId, Set.of());
            if (clientIds.isEmpty()) {
                log.warn("sendToUsers: no clients registered for userId={}", userId);
                return;
            }
            clientIds.forEach(clientId -> sendToClient(clientId, payload));
        });
    }

    private void sendToAllUsers(String payload) {
        requireUserTargeting();
        pruneKnownClients();
        clientIdsByUserId.values().forEach(clientIds -> clientIds.forEach(clientId -> sendToClient(clientId, payload)));
    }

    // -------------------------------------------------------------------------
    // Internal helpers
    // -------------------------------------------------------------------------

    private void sendToClient(String clientId, String payload) {
        SseEmitter emitter = emitters.get(clientId);
        if (emitter == null) {
            queuePendingEvent(clientId, payload, "no emitter registered");
            return;
        }
        if (!emitter.isOpen()) {
            emitters.remove(clientId, emitter);
            rememberClient(clientId);
            queuePendingEvent(clientId, payload, "emitter is closed");
            return;
        }
        sendToEmitter(clientId, emitter, payload, () -> log.debug("sendToClient: sent event to clientId={}", clientId));
    }

    private void sendToEmitter(String clientId, SseEmitter emitter, String payload, Runnable successCallback) {
        sendToEmitter(clientId, emitter, payload, successCallback,
                throwable -> handleSendFailure(clientId, emitter, payload, throwable));
    }

    private void sendToEmitter(String clientId, SseEmitter emitter, String payload, Runnable successCallback,
                               Consumer<Throwable> failureCallback) {
        CompletionStage<Void> sendResult;
        try {
            sendResult = emitter.send(payload);
        } catch (RuntimeException e) {
            sendResult = CompletableFuture.failedFuture(e);
        }
        if (sendResult == null) {
            sendResult = CompletableFuture.failedFuture(new IllegalStateException("SseEmitter.send returned null"));
        }
        sendResult.whenComplete((ignored, throwable) -> {
            if (throwable == null) {
                successCallback.run();
                return;
            }
            failureCallback.accept(throwable);
        });
    }

    private void handleSendFailure(String clientId, SseEmitter emitter, String payload, Throwable throwable) {
        Throwable cause = unwrapCompletionException(throwable);
        emitters.remove(clientId, emitter);
        rememberClient(clientId);
        closeQuietly(emitter);
        queuePendingEvent(clientId, payload, "send failed: " + cause.getMessage());
    }

    private void closeQuietly(SseEmitter emitter) {
        try {
            emitter.close();
        } catch (RuntimeException e) {
            log.debug("closeQuietly: ignoring failure while closing SSE emitter", e);
        }
    }

    private void queuePendingEvent(String clientId, String payload, String reason) {
        prunePendingEvents(clientId);
        pendingEventsByClientId.computeIfAbsent(clientId, ignored -> new ConcurrentLinkedQueue<>())
                .add(new PendingEvent(payload, System.currentTimeMillis() + PENDING_EVENT_TTL_MILLIS));
        log.debug("sendToClient: {} for clientId={}, queued event for reconnect", reason, clientId);
    }

    private void flushPendingEvents(String clientId, SseEmitter emitter) {
        Queue<PendingEvent> pendingEvents = pendingEventsByClientId.get(clientId);
        if (pendingEvents == null || pendingEvents.isEmpty()) {
            return;
        }
        if (!emitter.isOpen()) {
            return;
        }
        PendingEvent event = pendingEvents.peek();
        while (event != null && event.isExpired()) {
            pendingEvents.poll();
            event = pendingEvents.peek();
        }
        if (event == null) {
            pendingEventsByClientId.remove(clientId, pendingEvents);
            return;
        }
        PendingEvent currentEvent = event;
        sendToEmitter(clientId, emitter, currentEvent.payload(), () -> {
            pendingEvents.poll();
            log.debug("flushPendingEvents: sent pending event to clientId={}", clientId);
            if (pendingEvents.isEmpty()) {
                pendingEventsByClientId.remove(clientId, pendingEvents);
            } else if (emitters.get(clientId) == emitter && emitter.isOpen()) {
                flushPendingEvents(clientId, emitter);
            }
        }, throwable -> handleFlushSendFailure(clientId, emitter, throwable));
    }

    private void handleFlushSendFailure(String clientId, SseEmitter emitter, Throwable throwable) {
        Throwable cause = unwrapCompletionException(throwable);
        emitters.remove(clientId, emitter);
        rememberClient(clientId);
        closeQuietly(emitter);
        log.debug("flushPendingEvents: send failed for clientId={}, keeping pending event for reconnect: {}",
                clientId, cause.getMessage());
    }

    private void prunePendingEvents(String clientId) {
        Queue<PendingEvent> pendingEvents = pendingEventsByClientId.get(clientId);
        if (pendingEvents == null) {
            return;
        }
        pendingEvents.removeIf(PendingEvent::isExpired);
        if (pendingEvents.isEmpty()) {
            pendingEventsByClientId.remove(clientId, pendingEvents);
        }
    }

    private void rememberClient(String clientId) {
        knownClientExpiresAt.put(clientId, System.currentTimeMillis() + PENDING_EVENT_TTL_MILLIS);
    }

    private void pruneKnownClients() {
        long now = System.currentTimeMillis();
        knownClientExpiresAt.entrySet().removeIf(entry -> entry.getValue() < now);
        userIdByClientId.keySet().removeIf(clientId -> !knownClientExpiresAt.containsKey(clientId));
        clientIdsByUserId.entrySet().removeIf(entry -> {
            entry.getValue().removeIf(clientId -> !knownClientExpiresAt.containsKey(clientId));
            return entry.getValue().isEmpty();
        });
    }

    private String buildPayload(String eventKey) {
        return "data:" + eventKey + "\n\n";
    }

    private Throwable unwrapCompletionException(Throwable throwable) {
        if ((throwable instanceof CompletionException || throwable instanceof ExecutionException)
                && throwable.getCause() != null) {
            return throwable.getCause();
        }
        return throwable;
    }

    private void updateUserRegistration(String clientId, String userId) {
        unregisterUserMapping(clientId);
        if (userId == null || userId.isBlank()) {
            return;
        }
        userIdByClientId.put(clientId, userId);
        clientIdsByUserId.computeIfAbsent(userId, ignored -> ConcurrentHashMap.newKeySet()).add(clientId);
    }

    private void unregisterUserMapping(String clientId) {
        String previousUserId = userIdByClientId.remove(clientId);
        if (previousUserId == null) {
            return;
        }
        Set<String> clientIds = clientIdsByUserId.get(previousUserId);
        if (clientIds == null) {
            return;
        }
        clientIds.remove(clientId);
        if (clientIds.isEmpty()) {
            clientIdsByUserId.remove(previousUserId);
        }
    }

    private void requireUserTargeting() {
        if (userSecurityService instanceof DefaultUserSecurityService) {
            throw new UserTargetingNotAvailableException();
        }
    }

    private void validateEventKey(String eventKey) {
        if (!knownEventKeys.isEmpty() && !knownEventKeys.contains(eventKey)) {
            throw new IllegalArgumentException(
                    "Unknown SSE event key '" + eventKey + "'. " +
                            "Declare it via @RefreshOnUpdateEvents on a @Page or @Frontlet controller. " +
                            "Known keys: " + knownEventKeys);
        }
    }

    private record PendingEvent(String payload, long expiresAtMillis) {

        boolean isExpired() {
            return System.currentTimeMillis() > expiresAtMillis;
        }
    }
}
