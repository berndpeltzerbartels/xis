package one.xis.server;

import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

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

    public static RefreshEvent toAll(String eventKey) {
        return new RefreshEvent(eventKey, Set.of(RefreshTarget.all()));
    }

    public static RefreshEvent toClient(String eventKey, String clientId) {
        return new RefreshEvent(eventKey, Set.of(RefreshTarget.client(clientId)));
    }

    public static RefreshEvent toClients(String eventKey, String... clientIds) {
        return new RefreshEvent(eventKey, Set.of(RefreshTarget.clients(Arrays.asList(clientIds))));
    }

    public static RefreshEvent toUser(String eventKey, String userId) {
        return new RefreshEvent(eventKey, Set.of(RefreshTarget.user(userId)));
    }

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
