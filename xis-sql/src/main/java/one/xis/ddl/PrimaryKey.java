package one.xis.ddl;

import java.util.List;

public class PrimaryKey {
    private final Table table;
    private final List<Column> columns;

    PrimaryKey(Table table, List<Column> columns) {
        this.table = table;
        this.columns = List.copyOf(columns);
    }

    public Table table() {
        return table;
    }

    public List<Column> columns() {
        return columns;
    }
}
