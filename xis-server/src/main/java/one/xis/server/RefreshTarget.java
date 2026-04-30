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
public final class RefreshTarget {

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

    public static RefreshTarget all() {
        return new RefreshTarget(Type.ALL, Set.of());
    }

    public static RefreshTarget allUsers() {
        return new RefreshTarget(Type.ALL_USERS, Set.of());
    }

    public static RefreshTarget client(String clientId) {
        return clients(Set.of(requireValue(clientId, "clientId")));
    }

    public static RefreshTarget clients(String... clientIds) {
        return clients(Arrays.asList(clientIds));
    }

    public static RefreshTarget clients(Collection<String> clientIds) {
        return new RefreshTarget(Type.CLIENT, normalizeValues(clientIds, "clientIds"));
    }

    public static RefreshTarget user(String userId) {
        return users(Set.of(requireValue(userId, "userId")));
    }

    public static RefreshTarget users(String... userIds) {
        return users(Arrays.asList(userIds));
    }

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
