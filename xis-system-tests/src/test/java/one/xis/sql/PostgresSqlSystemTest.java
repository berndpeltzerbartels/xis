package one.xis.sql;

import org.junit.jupiter.api.BeforeAll;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import javax.sql.DataSource;
import java.sql.SQLException;

@Testcontainers
class PostgresSqlSystemTest extends SqlSystemTestSupport {
    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine");

    private static DataSource dataSource;

    @BeforeAll
    static void createDataSource() {
        dataSource = new DriverManagerDataSource(POSTGRES.getJdbcUrl(), POSTGRES.getUsername(), POSTGRES.getPassword());
    }

    @Override
    DataSource dataSource() {
        return dataSource;
    }

    @Override
    void createSchema() throws SQLException {
        try (var connection = dataSource.getConnection();
             var statement = connection.createStatement()) {
            statement.execute("drop procedure if exists add_five(integer)");
            statement.execute("drop function if exists double_value(integer)");
            statement.execute("drop table if exists people");
            statement.execute("create table people (id bigint primary key, first_name varchar(100) not null, notes text not null)");
            statement.execute("insert into people (id, first_name, notes) values (1, 'Ada', 'First note from clob'), (2, 'Grace', 'Second note')");
            statement.execute("create function double_value(value integer) returns integer language sql as 'select value * 2'");
            statement.execute("create procedure add_five(value integer, out result integer) language plpgsql as 'begin result := value + 5; end;'");
        }
    }

}
