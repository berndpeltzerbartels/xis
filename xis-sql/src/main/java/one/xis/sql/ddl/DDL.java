package one.xis.sql.ddl;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;
import javax.sql.DataSource;

public class DDL {
    private static final Pattern IDENTIFIER = Pattern.compile("[A-Za-z_][A-Za-z0-9_]*");
    private static final Set<String> RESERVED_WORDS = Set.of(
            "constraint", "foreign", "group", "index", "key", "order", "primary", "select", "table", "user");

    private final DataSource dataSource;
    private final List<Operation> operations = new ArrayList<>();
    private final Set<String> tableNames = new LinkedHashSet<>();

    public DDL(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    DDL() {
        this.dataSource = null;
    }

    public Table createTableIfNotExists(String name) {
        var table = new Table(name);
        if (!tableNames.add(table.name())) {
            throw new IllegalArgumentException("Duplicate table " + table.name());
        }
        operations.add(new TableOperation(table));
        return table;
    }

    public DDL dropTableIfExists(String tableName) {
        operations.add(new DropTableOperation(validateIdentifier(tableName, "table name")));
        return this;
    }

    public DDL sql(String sql) {
        if (sql == null || sql.isBlank()) {
            throw new IllegalArgumentException("sql must not be blank");
        }
        operations.add(new SqlOperation(sql.strip()));
        return this;
    }

    public DDL trigger(String sql) {
        return sql(sql);
    }

    public List<String> statements(SqlDialect dialect) {
        List<String> statements = new ArrayList<>();
        for (Operation operation : operations) {
            statements.addAll(operation.statements(dialect));
        }
        return List.copyOf(statements);
    }

    public void execute() {
        if (dataSource == null) {
            throw new IllegalStateException("DDL execution requires a DataSource");
        }
        SqlDialect dialect = SqlDialect.detect(dataSource);
        List<String> statements = statements(dialect);
        try (var connection = dataSource.getConnection();
             var statement = connection.createStatement()) {
            for (String sql : statements) {
                statement.execute(sql);
            }
        } catch (SQLException e) {
            throw new IllegalStateException("DDL execution failed", e);
        }
    }

    static String validateIdentifier(String identifier, String label) {
        if (identifier == null || identifier.isBlank()) {
            throw new IllegalArgumentException(label + " must not be blank");
        }
        if (!IDENTIFIER.matcher(identifier).matches()) {
            throw new IllegalArgumentException(label + " must be a portable SQL identifier: " + identifier);
        }
        return identifier;
    }

    static String sqlIdentifier(SqlDialect dialect, String identifier) {
        if (!isReservedWord(identifier)) {
            return identifier;
        }
        return switch (dialect) {
            case H2, POSTGRESQL -> "\"" + identifier + "\"";
            case MARIADB -> "`" + identifier + "`";
        };
    }

    private static boolean isReservedWord(String identifier) {
        return RESERVED_WORDS.contains(identifier.toLowerCase(Locale.ROOT));
    }

    private interface Operation {
        List<String> statements(SqlDialect dialect);
    }

    private record TableOperation(Table table) implements Operation {
        @Override
        public List<String> statements(SqlDialect dialect) {
            return table.statements(dialect);
        }
    }

    private record DropTableOperation(String tableName) implements Operation {
        @Override
        public List<String> statements(SqlDialect dialect) {
            return List.of("drop table if exists " + sqlIdentifier(dialect, tableName));
        }
    }

    private record SqlOperation(String sql) implements Operation {
        @Override
        public List<String> statements(SqlDialect dialect) {
            return List.of(sql);
        }
    }
}
