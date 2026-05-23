package one.xis.sql;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.MariaDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
class MariaDbGeneratedKeysJdbcBehaviorTest {

    @Container
    static final MariaDBContainer<?> MARIADB = new MariaDBContainer<>("mariadb:11.4");

    private static DriverManagerDataSource dataSource;

    @BeforeAll
    static void createDataSource() {
        dataSource = new DriverManagerDataSource(MARIADB.getJdbcUrl(), MARIADB.getUsername(), MARIADB.getPassword());
    }

    @BeforeEach
    void createSchema() throws SQLException {
        try (var connection = dataSource.getConnection();
             var statement = connection.createStatement()) {
            statement.execute("drop table if exists generated_key_probe");
            statement.execute("""
                    create table generated_key_probe (
                        id bigint not null auto_increment primary key,
                        business_key varchar(100) unique not null,
                        label varchar(100) not null
                    )
                    """);
        }
    }

    @Test
    void upsertInsertReturnsGeneratedPrimaryKey() throws SQLException {
        long id = upsert("customer-1", "Ada");

        assertThat(id).isPositive();
        assertThat(label(id)).isEqualTo("Ada");
    }

    @Test
    void upsertUpdateReturnsExistingPrimaryKey() throws SQLException {
        long id = upsert("customer-1", "Ada");
        long updatedId = upsert("customer-1", "Grace");

        assertThat(updatedId).isEqualTo(id);
        assertThat(label(id)).isEqualTo("Grace");
    }

    private long upsert(String businessKey, String label) throws SQLException {
        try (var connection = dataSource.getConnection();
             var statement = connection.prepareStatement(upsertSql(), new String[]{"id"})) {
            statement.setString(1, businessKey);
            statement.setString(2, label);
            assertThat(statement.executeUpdate()).isPositive();
            try (var keys = statement.getGeneratedKeys()) {
                assertThat(keys.next()).isTrue();
                assertThat(keys.getMetaData().getColumnCount()).isGreaterThanOrEqualTo(1);
                assertThat(keys.getMetaData().getColumnName(1)).isNotBlank();
                long id = keys.getLong(1);
                assertThat(keys.next()).isFalse();
                return id;
            }
        }
    }

    private String label(long id) throws SQLException {
        try (var connection = dataSource.getConnection();
             var statement = connection.prepareStatement("select label from generated_key_probe where id = ?")) {
            statement.setLong(1, id);
            try (var resultSet = statement.executeQuery()) {
                assertThat(resultSet.next()).isTrue();
                return resultSet.getString(1);
            }
        }
    }

    private String upsertSql() {
        return """
                insert into generated_key_probe (business_key, label)
                values (?, ?)
                on duplicate key update id = last_insert_id(id), label = values(label)
                """;
    }
}
