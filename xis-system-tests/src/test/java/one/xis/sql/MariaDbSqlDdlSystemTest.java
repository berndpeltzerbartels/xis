package one.xis.sql;

import org.junit.jupiter.api.BeforeAll;
import org.testcontainers.containers.MariaDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import javax.sql.DataSource;

@Testcontainers
class MariaDbSqlDdlSystemTest extends SqlDdlSystemTestSupport {
    @Container
    static final MariaDBContainer<?> MARIADB = new MariaDBContainer<>("mariadb:11.4");

    private static DataSource dataSource;

    @BeforeAll
    static void createDataSource() {
        dataSource = new DriverManagerDataSource(MARIADB.getJdbcUrl(), MARIADB.getUsername(), MARIADB.getPassword());
    }

    @Override
    DataSource dataSource() {
        return dataSource;
    }
}
