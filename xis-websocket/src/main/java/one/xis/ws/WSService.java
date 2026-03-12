package one.xis.ws;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import one.xis.context.Component;
import one.xis.context.Init;
import one.xis.gson.GsonProvider;
import one.xis.server.ClientConfigService;

import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;


@Slf4j
@Component
@RequiredArgsConstructor
public class WSService {

    /**
     * How long unacknowledged push events are kept in memory for offline clients.
     * After this duration the events are discarded – the client is considered gone.
     */
    static final Duration PENDING_EVENT_TTL = Duration.ofMinutes(30);

    private final GsonProvider gsonProvider;
    private final ClientConfigService clientConfigService;
    private final Map<String, WSEmitter> emitterMap = new ConcurrentHashMap<>();

    /**
     * Pending update-event messages per clientId, keyed by eventId.
     * An event is added here when it is sent and removed only when the client
     * confirms delivery via {@code push-ack}. On reconnect all still-pending
     * events are re-sent – duplicate delivery is acceptable (idempotent refresh).
     */
    private final Map<String, Map<String, WSUpdateEventMessage>> pendingRefreshEvents = new ConcurrentHashMap<>();

    private final ScheduledExecutorService cleanupScheduler =
            Executors.newSingleThreadScheduledExecutor(r -> {
                var t = new Thread(r, "ws-pending-cleanup");
                t.setDaemon(true);
                return t;
            });

    @Init
    void startCleanupScheduler() {
        clientConfigService.setPendingEventTtlSeconds(PENDING_EVENT_TTL.getSeconds());
        cleanupScheduler.scheduleWithFixedDelay(
                this::cleanupStalePendingEvents,
                1, 1, TimeUnit.HOURS);
        log.debug("startCleanupScheduler: pending-event TTL cleanup started (TTL={})", PENDING_EVENT_TTL);
    }

    /**
     * Removes push events that have exceeded {@link #PENDING_EVENT_TTL}.
     * If all events for a client are expired the client entry itself is removed.
     * Called periodically by the cleanup scheduler.
     */
    void cleanupStalePendingEvents() {
        var cutoff = Instant.now().minus(PENDING_EVENT_TTL);
        var removedEvents = 0;
        var removedClients = 0;
        for (var entry : pendingRefreshEvents.entrySet()) {
            var clientId = entry.getKey();
            var events = entry.getValue();
            var before = events.size();
            events.values().removeIf(msg -> msg.getCreatedAt().isBefore(cutoff));
            removedEvents += before - events.size();
            if (events.isEmpty()) {
                pendingRefreshEvents.remove(clientId);
                removedClients++;
            }
        }
        if (removedEvents > 0) {
            log.info("cleanupStalePendingEvents: removed {} expired event(s) for {} client(s)",
                    removedEvents, removedClients);
        } else {
            log.debug("cleanupStalePendingEvents: nothing to remove");
        }
    }

    public void processRequest(String message, WSEmitter emitter) {
        var requestJsonObject = gsonProvider.getGson().fromJson(message, JsonObject.class);
        var clientId = clientId(requestJsonObject);
        var requestType = requestType(requestJsonObject);
        log.debug("processRequest: clientId={} type={} emitterOpen={}",
                clientId, requestType, emitter.isOpen());
        try {
            switch (requestType) {
                case CONNECT -> processConnectMessage(clientId, emitter);
                case RECONNECT -> processReconnectMessage(clientId, emitter);
                case PING -> processPing(clientId, emitter);
                case PUSH_ACK -> processPushAck(clientId, requestJsonObject);
                default -> throw new IllegalArgumentException("requestType: " + requestType);
            }
        } catch (Exception e) {
            log.error("processRequest: error clientId={}: {}", clientId, e.getMessage(), e);
        }
    }

    private String clientId(JsonObject requestJsonObject) {
        return Optional.ofNullable(requestJsonObject.get("clientId"))
                .map(JsonElement::getAsString)
                .orElseThrow(WSMessageFormatException::new);
    }

    private WSRequestType requestType(JsonObject requestJsonObject) {
        return Optional.ofNullable(requestJsonObject.get("request-type"))
                .map(JsonElement::getAsString)
                .map(WSRequestType::fromValue)
                .orElseThrow(WSMessageFormatException::new);
    }

    private void processPing(String clientId, WSEmitter emitter) {
        log.debug("processPing: clientId={} – updating emitter and sending PONG", clientId);
        registerEmitter(clientId, emitter);
        emitter.send(new WSPongMessage());
    }

    private void processPushAck(String clientId, JsonObject requestJsonObject) {
        var eventId = requestJsonObject.get("eventId").getAsString();
        log.debug("processPushAck: clientId={} eventId={}", clientId, eventId);
        var pending = pendingRefreshEvents.get(clientId);
        if (pending != null) {
            pending.remove(eventId);
            log.debug("processPushAck: removed eventId={} from pending for clientId={}", eventId, clientId);
        }
    }

    public Optional<WSEmitter> getEmitter(String clientId) {
        return Optional.ofNullable(emitterMap.get(clientId));
    }

    public Collection<WSEmitter> getAllEmitters() {
        return emitterMap.values();
    }

    public void unregisterSession(String clientId, Object channel) {
        var current = emitterMap.get(clientId);
        if (current != null && !current.isChannel(channel)) {
            log.debug("unregisterSession: clientId={} – ignoring close of stale channel, current channel is different", clientId);
            return;
        }
        var pending = pendingRefreshEvents.get(clientId);
        log.debug("unregisterSession: clientId={} pendingEvents={}",
                clientId, pending != null ? pending.keySet() : "[]");
        emitterMap.remove(clientId);
        // pendingRefreshEvents intentionally kept – events are re-sent on reconnect
        // and removed only after the client sends a push-ack.
    }

    public void removeClosedSessions(Predicate<WSEmitter> isClosed) {
        log.debug("removing closed sessions");
        emitterMap.entrySet().removeIf(entry -> isClosed.test(entry.getValue()));
    }

    /**
     * Sends an update-event push message to a specific client.
     * The client will reload all pages/widgets annotated with
     * {@code @RefreshOnUpdateEvents} for the given key.
     *
     * @param clientId       the target client
     * @param updateEventKey the event key to fire
     */
    public void sendUpdateEvent(String clientId, String updateEventKey) {
        var message = new WSUpdateEventMessage(updateEventKey);
        pendingRefreshEvents
                .computeIfAbsent(clientId, k -> new ConcurrentHashMap<>())
                .put(message.getEventId(), message);
        var emitter = emitterMap.get(clientId);
        if (emitter != null) {
            flushPendingRefreshEvents(clientId, emitter);
        } else {
            log.debug("sendUpdateEvent: clientId={} offline, buffering event key='{}' eventId={}",
                    clientId, updateEventKey, message.getEventId());
        }
    }

    /**
     * Broadcasts an update-event push message to ALL connected clients.
     * Clients that are currently offline receive the event buffered and
     * get it delivered on their next reconnect.
     *
     * @param refreshEvent the event containing the updateEventKey and the list of clientIds to send the event to
     */
    public void broadcastUpdateEvent(RefreshEvent refreshEvent) {
        refreshEvent.getClientIds().parallelStream()
                .forEach(clientId -> sendUpdateEvent(clientId, refreshEvent.getEventKey()));
    }

    public void broadcastToAllClients(String updateEventKey) {
        log.debug("broadcastToAllClients: key='{}' connectedClients={}", updateEventKey, emitterMap.size());
        emitterMap.keySet().parallelStream()
                .forEach(clientId -> sendUpdateEvent(clientId, updateEventKey));
    }

    private void registerEmitter(String clientId, WSEmitter newEmitter) {
        var old = emitterMap.put(clientId, newEmitter);
        log.debug("registerEmitter: clientId={} newEmitter=@{} oldEmitter=@{}",
                clientId,
                Integer.toHexString(System.identityHashCode(newEmitter)),
                old != null ? Integer.toHexString(System.identityHashCode(old)) : "none");
    }

    private void processConnectMessage(String clientId, WSEmitter emitter) {
        log.debug("processConnectMessage: clientId={}", clientId);
        registerEmitter(clientId, emitter);
        flushPendingRefreshEvents(clientId, emitter);
    }

    private void processReconnectMessage(String clientId, WSEmitter emitter) {
        var pending = pendingRefreshEvents.get(clientId);
        log.debug("processReconnectMessage: clientId={} pendingEvents={}",
                clientId, pending != null ? pending.keySet() : "[]");
        registerEmitter(clientId, emitter);
        flushPendingRefreshEvents(clientId, emitter);
    }

    /**
     * Re-sends all not-yet-acknowledged push messages to a client that (re)connected.
     * Events stay in the buffer until the client confirms with push-ack.
     */
    private void flushPendingRefreshEvents(String clientId, WSEmitter emitter) {
        var pending = pendingRefreshEvents.get(clientId);
        if (pending == null || pending.isEmpty()) {
            log.debug("flushPendingEvents: clientId={} – nothing to flush", clientId);
            return;
        }
        log.debug("flushPendingEvents: clientId={} re-sending {} unacknowledged event(s)", clientId, pending.size());
        for (var message : pending.values()) {
            log.debug("flushPendingEvents: clientId={} re-sending key='{}' eventId={}",
                    clientId, message.getUpdateEventKey(), message.getEventId());
            emitter.send(message);
        }
    }
}
