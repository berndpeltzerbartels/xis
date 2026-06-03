package one.xis.ddl;

import one.xis.context.Component;
import one.xis.context.Init;
import one.xis.context.Inject;
import one.xis.sql.DataSourceProvider;

import javax.sql.DataSource;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Component
class DDLChangeSetRunner {
    static final String HISTORY_TABLE = "__xis_schema_change";

    private final DataSourceProvider dataSourceProvider;

    @Inject(annotatedWith = ChangeSet.class)
    private Collection<Object> changeSets = List.of();

    DDLChangeSetRunner(DataSourceProvider dataSourceProvider) {
        this.dataSourceProvider = Objects.requireNonNull(dataSourceProvider, "dataSourceProvider must not be null");
    }

    @Init
    void run() {
        run(changeSets);
    }

    void run(Collection<?> changeSetInstances) {
        var plan = ChangePlan.create(changeSetInstances);
        if (plan.changeSets().isEmpty()) {
            return;
        }
        try (var connection = dataSourceProvider.dataSource().getConnection()) {
            var autoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);
            try {
                var dialect = SqlDialect.detect(connection);
                ensureHistoryTable(connection, dialect);
                var executedChanges = readExecutedChanges(connection);
                validateExecutedChangesStillExist(executedChanges, plan);
                executePendingChanges(connection, dialect, plan, executedChanges);
                connection.commit();
            } catch (RuntimeException | SQLException e) {
                rollback(connection);
                throw e;
            } finally {
                connection.setAutoCommit(autoCommit);
            }
        } catch (SQLException e) {
            throw new IllegalStateException("DDL change set execution failed", e);
        }
    }

    private void ensureHistoryTable(Connection connection, SqlDialect dialect) {
        var ddl = new DDL();
        var table = ddl.createTableIfNotExists(HISTORY_TABLE);
        table.addColumn("change_set_id").varchar(255).notNull();
        table.addColumn("change_id").varchar(255).notNull();
        table.addColumn("change_set_class").varchar(500).notNull();
        table.addColumn("change_method").varchar(255).notNull();
        table.addColumn("executed_at").timestamp().notNull();
        table.setPrimaryKey(table.getColumn("change_set_id"), table.getColumn("change_id"));
        ddl.execute(connection, dialect);
    }

    private Set<ChangeKey> readExecutedChanges(Connection connection) throws SQLException {
        Set<ChangeKey> changes = new HashSet<>();
        try (var statement = connection.createStatement();
             var resultSet = statement.executeQuery("select change_set_id, change_id from " + HISTORY_TABLE)) {
            while (resultSet.next()) {
                changes.add(new ChangeKey(resultSet.getString(1), resultSet.getString(2)));
            }
        }
        return changes;
    }

    private void validateExecutedChangesStillExist(Set<ChangeKey> executedChanges, ChangePlan plan) {
        var knownChanges = plan.changeKeys();
        var missingChanges = executedChanges.stream()
                .filter(change -> !knownChanges.contains(change))
                .sorted()
                .map(ChangeKey::toString)
                .collect(Collectors.toList());
        if (!missingChanges.isEmpty()) {
            throw new IllegalStateException("Executed DDL changes no longer exist: " + String.join(", ", missingChanges));
        }
    }

    private void executePendingChanges(Connection connection, SqlDialect dialect, ChangePlan plan, Set<ChangeKey> executedChanges) {
        for (ChangeSetDescriptor changeSet : plan.changeSets()) {
            for (ChangeDescriptor change : changeSet.changes()) {
                var key = new ChangeKey(changeSet.id(), change.id());
                if (executedChanges.contains(key)) {
                    continue;
                }
                var ddl = new DDL();
                invokeChange(changeSet.instance(), change.method(), ddl);
                ddl.execute(connection, dialect);
                insertHistory(connection, changeSet, change);
                executedChanges.add(key);
            }
        }
    }

    private void invokeChange(Object instance, Method method, DDL ddl) {
        try {
            method.setAccessible(true);
            method.invoke(instance, ddl);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("Cannot invoke DDL change method: " + method, e);
        } catch (InvocationTargetException e) {
            var cause = e.getCause();
            if (cause instanceof RuntimeException runtimeException) {
                throw runtimeException;
            }
            if (cause instanceof Error error) {
                throw error;
            }
            throw new IllegalStateException("DDL change method failed: " + method, cause);
        }
    }

    private void insertHistory(Connection connection, ChangeSetDescriptor changeSet, ChangeDescriptor change) {
        try (var statement = connection.prepareStatement("insert into " + HISTORY_TABLE
                + " (change_set_id, change_id, change_set_class, change_method, executed_at) values (?, ?, ?, ?, ?)")) {
            statement.setString(1, changeSet.id());
            statement.setString(2, change.id());
            statement.setString(3, changeSet.type().getName());
            statement.setString(4, change.method().getName());
            statement.setTimestamp(5, Timestamp.from(Instant.now()));
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("Could not record DDL change " + changeSet.id() + "/" + change.id(), e);
        }
    }

    private void rollback(Connection connection) {
        try {
            connection.rollback();
        } catch (SQLException ignored) {
            // The original error is more useful than a failed rollback.
        }
    }

    record ChangeKey(String changeSetId, String changeId) implements Comparable<ChangeKey> {
        @Override
        public int compareTo(ChangeKey other) {
            var changeSetComparison = changeSetId.compareTo(other.changeSetId);
            if (changeSetComparison != 0) {
                return changeSetComparison;
            }
            return changeId.compareTo(other.changeId);
        }

        @Override
        public String toString() {
            return changeSetId + "/" + changeId;
        }
    }

    record ChangeDescriptor(String id, Method method) {
    }

    record ChangeSetDescriptor(String id, Class<?> type, Class<?> previous, Object instance,
                               List<ChangeDescriptor> changes) {
    }

    record ChangePlan(List<ChangeSetDescriptor> changeSets) {
        static ChangePlan create(Collection<?> changeSetInstances) {
            if (changeSetInstances == null || changeSetInstances.isEmpty()) {
                return new ChangePlan(List.of());
            }
            Map<Class<?>, ChangeSetDescriptor> descriptorsByClass = new LinkedHashMap<>();
            Map<String, Class<?>> classesById = new HashMap<>();
            for (Object instance : changeSetInstances) {
                if (instance == null) {
                    throw new IllegalStateException("ChangeSet instance must not be null");
                }
                var descriptor = describe(instance);
                var type = descriptor.type();
                if (descriptorsByClass.putIfAbsent(type, descriptor) != null) {
                    throw new IllegalStateException("Duplicate ChangeSet class: " + type.getName());
                }
                var previousClass = classesById.putIfAbsent(descriptor.id(), type);
                if (previousClass != null) {
                    throw new IllegalStateException("Duplicate ChangeSet id '" + descriptor.id() + "' on "
                            + previousClass.getName() + " and " + type.getName());
                }
            }
            return new ChangePlan(sort(descriptorsByClass));
        }

        Set<ChangeKey> changeKeys() {
            Set<ChangeKey> keys = new HashSet<>();
            for (ChangeSetDescriptor changeSet : changeSets) {
                for (ChangeDescriptor change : changeSet.changes()) {
                    keys.add(new ChangeKey(changeSet.id(), change.id()));
                }
            }
            return keys;
        }

        private static ChangeSetDescriptor describe(Object instance) {
            var type = changeSetType(instance);
            var annotation = type.getAnnotation(ChangeSet.class);
            if (annotation == null) {
                throw new IllegalStateException("Missing @ChangeSet on " + type.getName());
            }
            if (annotation.value().isBlank()) {
                throw new IllegalStateException("@ChangeSet id must not be blank on " + type.getName());
            }
            return new ChangeSetDescriptor(annotation.value(), type, annotation.previous(), instance, describeChanges(type));
        }

        private static Class<?> changeSetType(Object instance) {
            Class<?> type = instance.getClass();
            while (type != null && type != Object.class) {
                if (type.getAnnotation(ChangeSet.class) != null) {
                    return type;
                }
                type = type.getSuperclass();
            }
            return instance.getClass();
        }

        private static List<ChangeDescriptor> describeChanges(Class<?> type) {
            Map<String, Method> methodsById = new HashMap<>();
            List<ChangeDescriptor> changes = new ArrayList<>();
            for (Method method : type.getDeclaredMethods()) {
                var annotation = method.getAnnotation(Change.class);
                if (annotation == null) {
                    continue;
                }
                validateChangeMethod(type, method, annotation);
                var previousMethod = methodsById.putIfAbsent(annotation.value(), method);
                if (previousMethod != null) {
                    throw new IllegalStateException("Duplicate @Change id '" + annotation.value() + "' in " + type.getName());
                }
                changes.add(new ChangeDescriptor(annotation.value(), method));
            }
            changes.sort(Comparator.comparing(ChangeDescriptor::id).thenComparing(change -> change.method().getName()));
            return List.copyOf(changes);
        }

        private static void validateChangeMethod(Class<?> type, Method method, Change annotation) {
            if (annotation.value().isBlank()) {
                throw new IllegalStateException("@Change id must not be blank on " + type.getName() + "." + method.getName());
            }
            if (Modifier.isStatic(method.getModifiers())) {
                throw new IllegalStateException("@Change method must not be static: " + type.getName() + "." + method.getName());
            }
            if (method.getReturnType() != Void.TYPE) {
                throw new IllegalStateException("@Change method must return void: " + type.getName() + "." + method.getName());
            }
            var parameters = method.getParameterTypes();
            if (parameters.length != 1 || parameters[0] != DDL.class) {
                throw new IllegalStateException("@Change method must have exactly one DDL parameter: "
                        + type.getName() + "." + method.getName());
            }
        }

        private static List<ChangeSetDescriptor> sort(Map<Class<?>, ChangeSetDescriptor> descriptorsByClass) {
            var roots = descriptorsByClass.values().stream()
                    .filter(descriptor -> descriptor.previous() == ChangeSet.None.class)
                    .toList();
            if (roots.isEmpty()) {
                throw new IllegalStateException("Cyclic ChangeSet dependency detected: no root ChangeSet found");
            }
            if (roots.size() > 1) {
                throw new IllegalStateException("Exactly one root ChangeSet without previous is required, found " + roots.size());
            }
            Map<Class<?>, List<ChangeSetDescriptor>> nextByPrevious = new HashMap<>();
            for (ChangeSetDescriptor descriptor : descriptorsByClass.values()) {
                if (descriptor.previous() == ChangeSet.None.class) {
                    continue;
                }
                var previous = descriptorsByClass.get(descriptor.previous());
                if (previous == null) {
                    var previousType = descriptor.previous();
                    if (previousType.getAnnotation(ChangeSet.class) == null) {
                        throw new IllegalStateException("Previous class is not a ChangeSet: " + previousType.getName());
                    }
                    throw new IllegalStateException("Previous ChangeSet is not included: " + previousType.getName());
                }
                nextByPrevious.computeIfAbsent(descriptor.previous(), ignored -> new ArrayList<>()).add(descriptor);
            }

            List<ChangeSetDescriptor> sorted = new ArrayList<>();
            Set<Class<?>> seen = new HashSet<>();
            var current = roots.get(0);
            while (current != null) {
                if (!seen.add(current.type())) {
                    throw new IllegalStateException("Cyclic ChangeSet dependency detected at " + current.type().getName());
                }
                sorted.add(current);
                var next = nextByPrevious.getOrDefault(current.type(), List.of());
                if (next.size() > 1) {
                    throw new IllegalStateException("ChangeSet chain must not fork after " + current.type().getName());
                }
                current = next.isEmpty() ? null : next.get(0);
            }
            if (sorted.size() != descriptorsByClass.size()) {
                var reached = sorted.stream().map(ChangeSetDescriptor::type).collect(Collectors.toSet());
                var unreachable = descriptorsByClass.keySet().stream()
                        .filter(type -> !reached.contains(type))
                        .map(Class::getName)
                        .sorted()
                        .collect(Collectors.joining(", "));
                throw new IllegalStateException("Unreachable or cyclic ChangeSets: " + unreachable);
            }
            return List.copyOf(sorted);
        }
    }
}
