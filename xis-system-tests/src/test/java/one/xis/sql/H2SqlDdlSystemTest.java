package one.xis.sql;

import org.h2.jdbcx.JdbcDataSource;

import javax.sql.DataSource;

class H2SqlDdlSystemTest extends SqlDdlSystemTestSupport {
    private final JdbcDataSource dataSource = newDataSource();

    @Override
    DataSource dataSource() {
        return dataSource;
    }

    private static JdbcDataSource newDataSource() {
        var dataSource = new JdbcDataSource();
        dataSource.setURL("jdbc:h2:mem:sql-ddl-system-test;DB_CLOSE_DELAY=-1");
        return dataSource;
    }
}
