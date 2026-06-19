package test.xis.systemtests.changeset.xis;

import one.xis.context.AppContext;
import one.xis.context.Component;
import one.xis.ddl.Change;
import one.xis.ddl.ChangeSet;
import one.xis.ddl.DDL;
import one.xis.sql.DataSourceProvider;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;

class XisChangeSetDiscoverySystemTest {

    @Test
    void xisContextDiscoversAndRunsChangeSetsWithoutAdditionalComponentAnnotation() throws SQLException {
        var dataSource = dataSource("xis-changeset-discovery");
        var context = AppContext.builder()
                .withComponentAnnotation(Component.class)
                .withSingleton(dataSource)
                .withBasePackageClass(DataSourceProvider.class)
                .withPackage(ChangeSet.class.getPackageName())
                .withPackage(getClass().getPackageName())
                .build();

        assertThat(context.getSingleton(XisDiscoveredChangeSet.class)).isNotNull();
        assertThat(count(dataSource, "select count(*) from xis_discovered_change")).isEqualTo(1);
    }

    private JdbcDataSource dataSource(String name) {
        var dataSource = new JdbcDataSource();
        dataSource.setURL("jdbc:h2:mem:" + name + ";DB_CLOSE_DELAY=-1");
        return dataSource;
    }

    private int count(JdbcDataSource dataSource, String sql) throws SQLException {
        try (var connection = dataSource.getConnection();
             var statement = connection.createStatement();
             var resultSet = statement.executeQuery(sql)) {
            assertThat(resultSet.next()).isTrue();
            return resultSet.getInt(1);
        }
    }
}

@ChangeSet("xis-discovered")
class XisDiscoveredChangeSet {

    @Change("001-create")
    void create(DDL ddl) {
        var table = ddl.createTableIfNotExists("xis_discovered_change");
        table.addColumn("id").bigint().primaryKey();
    }

    @Change("002-insert")
    void insert(DDL ddl) {
        ddl.sql("insert into xis_discovered_change (id) values (1)");
    }
}
