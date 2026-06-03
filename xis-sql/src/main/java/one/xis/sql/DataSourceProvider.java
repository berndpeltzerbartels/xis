package one.xis.sql;

import lombok.RequiredArgsConstructor;

import javax.sql.DataSource;

@RequiredArgsConstructor
public class DataSourceProvider {
    private final DataSource dataSource;

    public DataSource dataSource() {
        return dataSource;
    }
}
