package one.xis.sql;

import one.xis.sql.ddl.Column;
import one.xis.sql.ddl.ColumnType;
import one.xis.sql.ddl.DDL;
import one.xis.sql.ddl.PrimaryKey;
import one.xis.sql.ddl.Table;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

abstract class SqlDdlSystemTestSupport {

    abstract DataSource dataSource();

    @BeforeEach
    void setUp() {
        DDL ddl = new DDL(dataSource());
        ddl.dropTableIfExists("app_users");
        ddl.dropTableIfExists("tenants");

        Table tenants = ddl.createTableIfNotExists("tenants");
        Column tenantId = tenants.addColumn("id").type(ColumnType.BIGINT);
        tenants.addColumn("name").varchar(100).notNull();
        PrimaryKey tenantPk = tenants.setPrimaryKey(tenantId);

        Table users = ddl.createTableIfNotExists("app_users");
        Column userId = users.addColumn("id").type(ColumnType.BIGINT).generatedIdentity();
        Column userTenantId = users.addColumn("tenant_id").type(ColumnType.BIGINT).notNull();
        Column email = users.addColumn("email").varchar(100).notNull();
        users.setPrimaryKey(userId);
        users.addUniqueConstraint(userTenantId, email);
        users.addIndex(email);
        users.addForeignKey(tenantPk, userTenantId);
        ddl.sql("insert into tenants (id, name) values (1, 'Default')");
        ddl.execute();
    }

    @Test
    void createsSchemaWithForeignKeyAndUniqueConstraint() throws SQLException {
        try (var connection = dataSource().getConnection();
             var statement = connection.createStatement()) {
            assertThat(statement.executeUpdate("insert into app_users (tenant_id, email) values (1, 'ada@example.test')"))
                    .isEqualTo(1);

            assertThatThrownBy(() -> statement.executeUpdate("insert into app_users (tenant_id, email) values (999, 'grace@example.test')"))
                    .isInstanceOf(SQLException.class);

            assertThatThrownBy(() -> statement.executeUpdate("insert into app_users (tenant_id, email) values (1, 'ada@example.test')"))
                    .isInstanceOf(SQLException.class);
        }
    }

    @Test
    void executesRawSqlStatements() throws SQLException {
        DDL ddl = new DDL(dataSource());
        ddl.sql("insert into app_users (tenant_id, email) values (1, 'raw@example.test')");

        ddl.execute();

        try (var connection = dataSource().getConnection();
             var statement = connection.createStatement();
             var resultSet = statement.executeQuery("select count(*) from app_users where email = 'raw@example.test'")) {
            assertThat(resultSet.next()).isTrue();
            assertThat(resultSet.getInt(1)).isEqualTo(1);
        }
    }
}
