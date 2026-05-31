package one.xis.sql.ddl;

import lombok.Data;

import java.util.Objects;

@Data
public class Column {
    private final Table table;
    private final String name;
    private ColumnType columnType;
    private boolean notNull;
    private boolean generatedIdentity;
    private Integer length;
    private Integer precision;
    private Integer scale;

    Column(Table table, String name) {
        this.table = Objects.requireNonNull(table, "table must not be null");
        this.name = DDL.validateIdentifier(name, "column name");
    }

    public Column type(ColumnType columnType) {
        this.columnType = Objects.requireNonNull(columnType, "columnType must not be null");
        this.length = null;
        this.precision = null;
        this.scale = null;
        return this;
    }

    public Column bigint() {
        return type(ColumnType.BIGINT);
    }

    public Column integer() {
        return type(ColumnType.INTEGER);
    }

    public Column varchar(int length) {
        if (length < 1) {
            throw new IllegalArgumentException("varchar length must be positive");
        }
        this.columnType = ColumnType.VARCHAR;
        this.length = length;
        this.precision = null;
        this.scale = null;
        return this;
    }

    public Column text() {
        return type(ColumnType.TEXT);
    }

    public Column bool() {
        return type(ColumnType.BOOLEAN);
    }

    public Column date() {
        return type(ColumnType.DATE);
    }

    public Column time() {
        return type(ColumnType.TIME);
    }

    public Column timestamp() {
        return type(ColumnType.TIMESTAMP);
    }

    public Column decimal() {
        return type(ColumnType.DECIMAL);
    }

    public Column decimal(int precision, int scale) {
        if (precision < 1) {
            throw new IllegalArgumentException("decimal precision must be positive");
        }
        if (scale < 0 || scale > precision) {
            throw new IllegalArgumentException("decimal scale must be between 0 and precision");
        }
        this.columnType = ColumnType.DECIMAL;
        this.precision = precision;
        this.scale = scale;
        this.length = null;
        return this;
    }

    public Column notNull() {
        this.notNull = true;
        return this;
    }

    public Column nullable() {
        this.notNull = false;
        return this;
    }

    public Column generatedIdentity() {
        this.generatedIdentity = true;
        return this;
    }

    public Column unique() {
        table.addUniqueConstraint(this);
        return this;
    }

    public Column index() {
        table.addIndex(this);
        return this;
    }

    public Column foreignKey(Column referencedColumn) {
        Objects.requireNonNull(referencedColumn, "referencedColumn must not be null");
        table.addForeignKey(referencedColumn.table().primaryKeyForSingleReferencedColumn(referencedColumn), this);
        return this;
    }

    public Column primaryKey() {
        table.setPrimaryKey(this);
        return this;
    }

    public Column addColumn(String name) {
        return table.addColumn(name);
    }

    public Table table() {
        return table;
    }

    public String name() {
        return name;
    }

    public ColumnType columnType() {
        return columnType;
    }

    Integer length() {
        return length;
    }

    Integer precision() {
        return precision;
    }

    Integer scale() {
        return scale;
    }

    void validate() {
        if (columnType == null) {
            throw new IllegalStateException("Column " + table.name() + "." + name + " has no type");
        }
        if (columnType.isLengthRequired() && length == null) {
            throw new IllegalStateException("Column " + table.name() + "." + name + " requires a length");
        }
        if (generatedIdentity && !columnType.isIntegerNumber()) {
            throw new IllegalStateException("Identity column " + table.name() + "." + name + " must be an integer type");
        }
    }

    String definition(SqlDialect dialect, boolean primaryKeyColumn) {
        validate();
        StringBuilder sql = new StringBuilder(DDL.sqlIdentifier(dialect, name)).append(' ').append(typeSql(dialect));
        if (generatedIdentity) {
            sql.append(identitySql(dialect));
        }
        if (notNull || primaryKeyColumn || generatedIdentity && dialect == SqlDialect.MARIADB) {
            sql.append(" not null");
        }
        return sql.toString();
    }

    boolean typeCompatibleWith(Column other) {
        validate();
        other.validate();
        if (columnType != other.columnType) {
            return false;
        }
        return Objects.equals(length, other.length)
                && Objects.equals(precision, other.precision)
                && Objects.equals(scale, other.scale);
    }

    private String typeSql(SqlDialect dialect) {
        return switch (columnType) {
            case BIGINT -> "bigint";
            case INTEGER -> dialect == SqlDialect.POSTGRESQL ? "integer" : "int";
            case VARCHAR -> "varchar(" + length + ")";
            case TEXT -> dialect == SqlDialect.H2 ? "clob" : "text";
            case BOOLEAN -> "boolean";
            case DATE -> "date";
            case TIME -> "time";
            case TIMESTAMP -> "timestamp";
            case DECIMAL -> precision == null ? "decimal" : "decimal(" + precision + ", " + scale + ")";
        };
    }

    private String identitySql(SqlDialect dialect) {
        return switch (dialect) {
            case H2, POSTGRESQL -> " generated by default as identity";
            case MARIADB -> " auto_increment";
        };
    }
}
