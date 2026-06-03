package test.xis.systemtests.changeset.spring;

import one.xis.ddl.Change;
import one.xis.ddl.ChangeSet;
import one.xis.ddl.DDL;
import one.xis.spring.SpringContextAdapter;
import org.junit.jupiter.api.Test;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

import javax.sql.DataSource;
import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;

class SpringChangeSetDiscoverySystemTest {

    @Test
    void springContextImportsAndRunsChangeSetsInXisContext() throws SQLException {
        try (var spring = new SpringApplicationBuilder(SpringChangeSetApplication.class)
                .web(WebApplicationType.NONE)
                .properties(
                        "xis.sql.url=jdbc:h2:mem:spring-changeset-discovery",
                        "xis.mongo.database=spring_changeset_discovery")
                .run()) {
            var adapter = spring.getBean(SpringContextAdapter.class);
            var dataSource = adapter.getContext().getSingleton(DataSource.class);

            assertThat(spring.getBean(SpringDiscoveredChangeSet.class)).isNotNull();
            assertThat(adapter.getContext().getSingleton(SpringDiscoveredChangeSet.class)).isNotNull();
            assertThat(count(dataSource, "select count(*) from spring_discovered_change")).isEqualTo(1);
        }
    }

    @SpringBootApplication(scanBasePackageClasses = SpringChangeSetDiscoverySystemTest.class)
    static class SpringChangeSetApplication {
    }

    private int count(DataSource dataSource, String sql) throws SQLException {
        try (var connection = dataSource.getConnection();
             var statement = connection.createStatement();
             var resultSet = statement.executeQuery(sql)) {
            assertThat(resultSet.next()).isTrue();
            return resultSet.getInt(1);
        }
    }
}

@ChangeSet("spring-discovered")
class SpringDiscoveredChangeSet {

    @Change("001-create")
    void create(DDL ddl) {
        var table = ddl.createTableIfNotExists("spring_discovered_change");
        table.addColumn("id").bigint().primaryKey();
    }

    @Change("002-insert")
    void insert(DDL ddl) {
        ddl.sql("insert into spring_discovered_change (id) values (1)");
    }
}
