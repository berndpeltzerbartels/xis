package one.xis.ddl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class Table {
    private final String name;
    private final Map<String, Column> columns = new LinkedHashMap<>();
    private final List<List<Column>> indexes = new ArrayList<>();
    private final List<List<Column>> uniqueConstraints = new ArrayList<>();
    private final List<ForeignKey> foreignKeys = new ArrayList<>();
    private PrimaryKey primaryKey;

    Table(String name) {
        this.name = DDL.validateIdentifier(name, "table name");
    }

    public String name() {
        return name;
    }

    public Column addColumn(String name) {
        var column = new Column(this, name);
        if (columns.putIfAbsent(column.name(), column) != null) {
            throw new IllegalArgumentException("Duplicate column " + this.name + "." + column.name());
        }
        return column;
    }

    public Column getColumn(String name) {
        String validatedName = DDL.validateIdentifier(name, "column name");
        Column column = columns.get(validatedName);
        if (column == null) {
            throw new IllegalArgumentException("Table " + this.name + " has no column " + validatedName);
        }
        return column;
    }

    public PrimaryKey setPrimaryKey(Column... pkColumns) {
        if (primaryKey != null) {
            throw new IllegalStateException("Table " + name + " already has a primary key");
        }
        List<Column> validatedColumns = validateColumns("primary key", pkColumns);
        primaryKey = new PrimaryKey(this, validatedColumns);
        return primaryKey;
    }

    public void addIndex(Column... columns) {
        indexes.add(validateColumns("index", columns));
    }

    public void addUniqueConstraint(Column... columns) {
        uniqueConstraints.add(validateColumns("unique constraint", columns));
    }

    public void addForeignKey(PrimaryKey referencedKey, Column... localColumns) {
        Objects.requireNonNull(referencedKey, "referencedKey must not be null");
        if (referencedKey.table() == this) {
            throw new IllegalArgumentException("Foreign key on " + name + " must not reference the same table");
        }
        List<Column> validatedLocalColumns = validateColumns("foreign key", localColumns);
        List<Column> referencedColumns = referencedKey.columns();
        if (validatedLocalColumns.size() != referencedColumns.size()) {
            throw new IllegalArgumentException("Foreign key column count must match referenced primary key column count");
        }
        for (int i = 0; i < validatedLocalColumns.size(); i++) {
            Column localColumn = validatedLocalColumns.get(i);
            Column referencedColumn = referencedColumns.get(i);
            if (!localColumn.typeCompatibleWith(referencedColumn)) {
                throw new IllegalArgumentException("Foreign key column " + name + "." + localColumn.name()
                        + " is not compatible with " + referencedKey.table().name() + "." + referencedColumn.name());
            }
        }
        foreignKeys.add(new ForeignKey(referencedKey, validatedLocalColumns));
    }

    PrimaryKey primaryKeyForSingleReferencedColumn(Column referencedColumn) {
        Objects.requireNonNull(referencedColumn, "referencedColumn must not be null");
        if (referencedColumn.table() != this) {
            throw new IllegalArgumentException("Column " + referencedColumn.name() + " does not belong to table " + name);
        }
        if (primaryKey == null) {
            throw new IllegalArgumentException("Table " + name + " has no primary key");
        }
        if (primaryKey.columns().size() != 1 || primaryKey.columns().get(0) != referencedColumn) {
            throw new IllegalArgumentException("Column " + name + "." + referencedColumn.name()
                    + " is not the single-column primary key of table " + name);
        }
        return primaryKey;
    }

    List<String> statements(SqlDialect dialect) {
        validate();
        List<String> statements = new ArrayList<>();
        statements.add(createTableStatement(dialect));
        for (List<Column> index : indexes) {
            statements.add("create index if not exists " + DDL.sqlIdentifier(dialect, constraintName("ix", index))
                    + " on " + DDL.sqlIdentifier(dialect, name) + " (" + columnNames(dialect, index) + ")");
        }
        return statements;
    }

    private void validate() {
        if (columns.isEmpty()) {
            throw new IllegalStateException("Table " + name + " has no columns");
        }
        columns.values().forEach(Column::validate);
    }

    private String createTableStatement(SqlDialect dialect) {
        List<String> parts = columns.values().stream()
                .map(column -> column.definition(dialect, isPrimaryKeyColumn(column)))
                .collect(Collectors.toCollection(ArrayList::new));
        if (primaryKey != null) {
            parts.add("constraint " + DDL.sqlIdentifier(dialect, constraintName("pk", primaryKey.columns()))
                    + " primary key (" + columnNames(dialect, primaryKey.columns()) + ")");
        }
        for (List<Column> uniqueConstraint : uniqueConstraints) {
            parts.add("constraint " + DDL.sqlIdentifier(dialect, constraintName("uq", uniqueConstraint))
                    + " unique (" + columnNames(dialect, uniqueConstraint) + ")");
        }
        for (ForeignKey foreignKey : foreignKeys) {
            parts.add("constraint " + DDL.sqlIdentifier(dialect, constraintName("fk_" + foreignKey.referencedKey().table().name(), foreignKey.localColumns()))
                    + " foreign key (" + columnNames(dialect, foreignKey.localColumns()) + ") references "
                    + DDL.sqlIdentifier(dialect, foreignKey.referencedKey().table().name()) + " ("
                    + columnNames(dialect, foreignKey.referencedKey().columns()) + ")");
        }
        return "create table if not exists " + DDL.sqlIdentifier(dialect, name) + " (" + String.join(", ", parts) + ")";
    }

    private List<Column> validateColumns(String purpose, Column... columns) {
        if (columns == null || columns.length == 0) {
            throw new IllegalArgumentException("At least one column is required for " + purpose);
        }
        List<Column> result = Arrays.asList(columns);
        for (Column column : result) {
            if (column == null) {
                throw new IllegalArgumentException("Column for " + purpose + " must not be null");
            }
            if (column.table() != this) {
                throw new IllegalArgumentException("Column " + column.name() + " does not belong to table " + name);
            }
            if (!this.columns.containsValue(column)) {
                throw new IllegalArgumentException("Column " + column.name() + " is not registered in table " + name);
            }
            column.validate();
        }
        if (result.stream().map(Column::name).distinct().count() != result.size()) {
            throw new IllegalArgumentException("Duplicate columns are not allowed for " + purpose);
        }
        return List.copyOf(result);
    }

    private boolean isPrimaryKeyColumn(Column column) {
        return primaryKey != null && primaryKey.columns().contains(column);
    }

    private static String columnNames(SqlDialect dialect, List<Column> columns) {
        return columns.stream()
                .map(column -> DDL.sqlIdentifier(dialect, column.name()))
                .collect(Collectors.joining(", "));
    }

    private String constraintName(String prefix, List<Column> columns) {
        return prefix + "_" + name + "_" + columns.stream().map(Column::name).collect(Collectors.joining("_"));
    }

    private record ForeignKey(PrimaryKey referencedKey, List<Column> localColumns) {
    }
}
