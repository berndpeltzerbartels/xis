package one.xis.sql;

import one.xis.context.AppContext;
import one.xis.sql.proxyapp.ContextBuildService;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class SqlProxyFactoryContextBuildTest {

    @Test
    void buildsContextWithSqlInfrastructurePackageAndSeparateApplicationPackage() throws SQLException {
        var dataSource = dataSource();

        var context = AppContext.builder()
                .withSingleton(dataSource)
                .withPackage("one.xis.sql")
                .withPackage(ContextBuildService.class.getPackageName())
                .build();

        assertNotNull(context.getSingleton(TransactionManager.class));
        assertNotNull(context.getSingleton(SQLRepositoryProxyFactory.class));
        assertEquals("Ada", context.getSingleton(ContextBuildService.class).nameById(1L));
    }

    private JdbcDataSource dataSource() throws SQLException {
        var dataSource = new JdbcDataSource();
        dataSource.setURL("jdbc:h2:mem:sql-proxy-context-build;DB_CLOSE_DELAY=-1");
        try (var connection = dataSource.getConnection();
             var statement = connection.createStatement()) {
            statement.execute("drop table if exists people");
            statement.execute("create table people (id bigint primary key, first_name varchar(100))");
            statement.execute("insert into people values (1, 'Ada')");
        }
        return dataSource;
    }
}
