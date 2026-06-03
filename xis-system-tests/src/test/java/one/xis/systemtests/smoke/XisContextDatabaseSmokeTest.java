package one.xis.systemtests.smoke;

import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import one.xis.context.AppContext;
import one.xis.sql.DriverManagerDataSource;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.MariaDBContainer;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import javax.sql.DataSource;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
class XisContextDatabaseSmokeTest {
    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine");

    @Container
    static final MariaDBContainer<?> MARIADB = new MariaDBContainer<>("mariadb:11.4");

    @Container
    static final MongoDBContainer MONGODB = new MongoDBContainer("mongo:7.0");

    private static DataSource postgresDataSource;
    private static DataSource mariaDbDataSource;
    private static MongoDatabase mongoDatabase;

    @BeforeAll
    static void createDatabases() {
        postgresDataSource = new DriverManagerDataSource(
                POSTGRES.getJdbcUrl(),
                POSTGRES.getUsername(),
                POSTGRES.getPassword());
        mariaDbDataSource = new DriverManagerDataSource(
                MARIADB.getJdbcUrl(), MARIADB.getUsername(), MARIADB.getPassword());
        mongoDatabase = MongoClients.create(MONGODB.getReplicaSetUrl()).getDatabase("xis_context_smoke");
    }

    @Test
    void xisContextUsesPostgresRepository() {
        AppContext context = sqlContext(postgresDataSource);

        assertThat(context.getSingleton(SqlSmokeRepository.class).one()).isEqualTo(1);
    }

    @Test
    void xisContextUsesMariaDbRepository() {
        AppContext context = sqlContext(mariaDbDataSource);

        assertThat(context.getSingleton(SqlSmokeRepository.class).one()).isEqualTo(1);
    }

    private AppContext sqlContext(DataSource dataSource) {
        return AppContext.builder()
                .withPackage(getClass().getPackageName())
                .withSingletonClass("one.xis.sql.DataSourceFactory")
                .withSingletonClass("one.xis.sql.DataSourceConfiguration")
                .withSingletonClass("one.xis.sql.SqlConnectionProvider")
                .withSingletonClass("one.xis.sql.TransactionManager")
                .withSingletonClass("one.xis.sql.SQLRepositoryProxyFactory")
                .withSingleton(dataSource)
                .build();
    }

    @Test
    void xisContextUsesMongoRepository() {
        mongoDatabase.getCollection("smoke").drop();
        AppContext context = AppContext.builder()
                .withPackage(getClass().getPackageName())
                .withSingletonClass("one.xis.mongodb.MongoRepositoryProxyFactory")
                .withSingleton(mongoDatabase)
                .build();
        MongoSmokeRepository repository = context.getSingleton(MongoSmokeRepository.class);

        repository.save(new SmokeDocument("id-1", "stored"));
        assertThat(repository.findById("id-1")).map(document -> document.value).contains("stored");
    }
}
