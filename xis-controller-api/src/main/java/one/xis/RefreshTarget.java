package one.xis;

import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Recipient selection for a {@link RefreshEvent}.
 *
 * <p>Targets can address all connected clients, one or more concrete client ids,
 * or all clients that belong to one or more authenticated user ids.</p>
 */
@Getter
@EqualsAndHashCode
public final class RefreshTarget {

    /**
     * Target category used by the event dispatcher.
     */
    public enum Type {
        ALL,
        ALL_USERS,
        CLIENT,
        USER
    }

    private final Type type;
    private final Set<String> values;

    private RefreshTarget(Type type, Set<String> values) {
        this.type = Objects.requireNonNull(type, "type");
        this.values = Set.copyOf(values);
    }

    /**
     * Targets every connected browser client.
     */
    public static RefreshTarget all() {
        return new RefreshTarget(Type.ALL, Set.of());
    }

    /**
     * Targets all authenticated users. Anonymous clients are not selected by
     * this target.
     */
    public static RefreshTarget allUsers() {
        return new RefreshTarget(Type.ALL_USERS, Set.of());
    }

    /**
     * Targets one concrete browser client id.
     */
    public static RefreshTarget client(String clientId) {
        return clients(Set.of(requireValue(clientId, "clientId")));
    }

    /**
     * Targets multiple concrete browser client ids.
     */
    public static RefreshTarget clients(String... clientIds) {
        return clients(Arrays.asList(clientIds));
    }

    /**
     * Targets multiple concrete browser client ids.
     */
    public static RefreshTarget clients(Collection<String> clientIds) {
        return new RefreshTarget(Type.CLIENT, normalizeValues(clientIds, "clientIds"));
    }

    /**
     * Targets all connected clients of one authenticated user id.
     */
    public static RefreshTarget user(String userId) {
        return users(Set.of(requireValue(userId, "userId")));
    }

    /**
     * Targets all connected clients of multiple authenticated user ids.
     */
    public static RefreshTarget users(String... userIds) {
        return users(Arrays.asList(userIds));
    }

    /**
     * Targets all connected clients of multiple authenticated user ids.
     */
    public static RefreshTarget users(Collection<String> userIds) {
        return new RefreshTarget(Type.USER, normalizeValues(userIds, "userIds"));
    }

    private static Set<String> normalizeValues(Collection<String> values, String name) {
        Objects.requireNonNull(values, name);
        var normalized = new LinkedHashSet<String>();
        values.forEach(value -> normalized.add(requireValue(value, name)));
        if (normalized.isEmpty()) {
            throw new IllegalArgumentException(name + " must not be empty");
        }
        return normalized;
    }

    private static String requireValue(String value, String name) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(name + " must not be null or blank");
        }
        return value;
    }
}
