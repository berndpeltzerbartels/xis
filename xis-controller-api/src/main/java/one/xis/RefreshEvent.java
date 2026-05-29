package one.xis;

import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Server-side event that asks connected browser clients to refresh frontlets or
 * pages that listen for the event key.
 *
 * <p>Publish instances through {@link RefreshEventPublisher}. The event key is
 * matched against controllers annotated with {@link RefreshOnUpdateEvents};
 * targets decide which connected clients receive the notification.</p>
 */
@Getter
@EqualsAndHashCode
public final class RefreshEvent {

    private final String eventKey;
    private final Set<RefreshTarget> targets;

    public RefreshEvent(String eventKey, Collection<RefreshTarget> targets) {
        this.eventKey = requireEventKey(eventKey);
        Objects.requireNonNull(targets, "targets");
        if (targets.isEmpty()) {
            throw new IllegalArgumentException("targets must not be empty");
        }
        this.targets = Set.copyOf(new LinkedHashSet<>(targets));
    }

    /**
     * Creates an event for every connected client.
     */
    public static RefreshEvent toAll(String eventKey) {
        return new RefreshEvent(eventKey, Set.of(RefreshTarget.all()));
    }

    /**
     * Creates an event for one connected client id.
     */
    public static RefreshEvent toClient(String eventKey, String clientId) {
        return new RefreshEvent(eventKey, Set.of(RefreshTarget.client(clientId)));
    }

    /**
     * Creates an event for multiple connected client ids.
     */
    public static RefreshEvent toClients(String eventKey, String... clientIds) {
        return new RefreshEvent(eventKey, Set.of(RefreshTarget.clients(Arrays.asList(clientIds))));
    }

    /**
     * Creates an event for all clients of one authenticated user id.
     */
    public static RefreshEvent toUser(String eventKey, String userId) {
        return new RefreshEvent(eventKey, Set.of(RefreshTarget.user(userId)));
    }

    /**
     * Creates an event for all clients of multiple authenticated user ids.
     */
    public static RefreshEvent toUsers(String eventKey, String... userIds) {
        return new RefreshEvent(eventKey, Set.of(RefreshTarget.users(Arrays.asList(userIds))));
    }

    private static String requireEventKey(String eventKey) {
        if (eventKey == null || eventKey.isBlank()) {
            throw new IllegalArgumentException("eventKey must not be null or blank");
        }
        return eventKey;
    }
}
