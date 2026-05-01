package one.xis.server;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import one.xis.Page;
import one.xis.RefreshOnUpdateEvents;
import one.xis.Frontlet;
import one.xis.auth.token.DefaultUserSecurityService;
import one.xis.auth.token.UserSecurityService;
import one.xis.context.Component;
import one.xis.context.Init;
import one.xis.context.Inject;
import one.xis.http.SseEmitter;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Component
@RequiredArgsConstructor
public class SseService implements RefreshEventPublisher {

    /**
     * clientId -> active SSE emitter
     */
    private final Map<String, SseEmitter> emitters = new ConcurrentHashMap<>();
    private final Map<String, String> userIdByClientId = new ConcurrentHashMap<>();
    private final Map<String, Set<String>> clientIdsByUserId = new ConcurrentHashMap<>();
    private final UserSecurityService userSecurityService;

    /**
     * All valid event keys declared via @RefreshOnUpdateEvents across all controllers
     */
    private Set<String> knownEventKeys = Collections.emptySet();

    @Inject(annotatedWith = Page.class)
    private Collection<Object> pageControllers;

    @Inject(annotatedWith = Frontlet.class)
    private Collection<Object> widgetControllers;

    // -------------------------------------------------------------------------
    // Lifecycle
    // -------------------------------------------------------------------------

    @Init
    void initKnownEventKeys() {
        knownEventKeys = Stream.concat(pageControllers.stream(), widgetControllers.stream())
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
        updateUserRegistration(clientId, userId);
        log.debug("registerEmitter: clientId={}", clientId);
    }

    public void unregisterEmitter(String clientId, SseEmitter emitter) {
        emitters.remove(clientId, emitter);
        unregisterUserMapping(clientId);
        log.debug("unregisterEmitter: clientId={}", clientId);
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
        emitters.forEach((clientId, emitter) -> {
            if (emitter.isOpen()) {
                emitter.send(payload);
            } else {
                log.warn("sendToAll: skipping closed emitter for clientId={}", clientId);
            }
        });
    }

    private void sendToUsers(Collection<String> userIds, String payload) {
        requireUserTargeting();
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
        clientIdsByUserId.values().forEach(clientIds -> clientIds.forEach(clientId -> sendToClient(clientId, payload)));
    }

    // -------------------------------------------------------------------------
    // Internal helpers
    // -------------------------------------------------------------------------

    private void sendToClient(String clientId, String payload) {
        SseEmitter emitter = emitters.get(clientId);
        if (emitter == null) {
            log.warn("sendToClient: no emitter registered for clientId={}", clientId);
            return;
        }
        if (!emitter.isOpen()) {
            log.warn("sendToClient: emitter is closed for clientId={}", clientId);
            return;
        }
        emitter.send(payload);
        log.debug("sendToClient: sent event to clientId={}", clientId);
    }

    private String buildPayload(String eventKey) {
        return "data:" + eventKey + "\n\n";
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
}
