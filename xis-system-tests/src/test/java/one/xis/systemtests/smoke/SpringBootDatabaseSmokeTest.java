package one.xis.systemtests.smoke;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import one.xis.context.AppContext;
import org.junit.jupiter.api.Test;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.testcontainers.containers.MariaDBContainer;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import javax.sql.DataSource;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
class SpringBootDatabaseSmokeTest {
    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine");

    @Container
    static final MariaDBContainer<?> MARIADB = new MariaDBContainer<>("mariadb:11.4");

    @Container
    static final MongoDBContainer MONGODB = new MongoDBContainer("mongo:7.0");

    @Test
    void springBootConfiguredPostgresDataSourceWorksWithXisSql() {
        try (ConfigurableApplicationContext spring = sqlSpringContext(POSTGRES.getJdbcUrl(),
                POSTGRES.getUsername(), POSTGRES.getPassword())) {
            DataSource dataSource = spring.getBean(DataSource.class);

            assertThat(sqlRepository(dataSource).one()).isEqualTo(1);
        }
    }

    @Test
    void springBootConfiguredMariaDbDataSourceWorksWithXisSql() {
        try (ConfigurableApplicationContext spring = sqlSpringContext(MARIADB.getJdbcUrl(),
                MARIADB.getUsername(), MARIADB.getPassword())) {
            DataSource dataSource = spring.getBean(DataSource.class);

            assertThat(sqlRepository(dataSource).one()).isEqualTo(1);
        }
    }

    @Test
    void springBootConfiguredMongoClientWorksWithXisMongo() {
        try (ConfigurableApplicationContext spring = new SpringApplicationBuilder(MongoSpringApplication.class)
                .web(WebApplicationType.NONE)
                .properties(
                        "spring.data.mongodb.uri=" + MONGODB.getReplicaSetUrl(),
                        "spring.data.mongodb.database=xis_spring_smoke",
                        "spring.datasource.url=jdbc:h2:mem:xis-spring-mongo-smoke")
                .run()) {
            MongoClient client = spring.getBean(MongoClient.class);
            MongoDatabase database = client.getDatabase("xis_spring_smoke");
            database.getCollection("smoke").drop();
            MongoSmokeRepository repository = mongoRepository(database);

            repository.save(new SmokeDocument("id-1", "stored"));
            assertThat(repository.findById("id-1")).map(document -> document.value).contains("stored");
        }
    }

    private ConfigurableApplicationContext sqlSpringContext(String url, String username, String password) {
        return new SpringApplicationBuilder(SqlSpringApplication.class)
                .web(WebApplicationType.NONE)
                .properties(
                        "spring.datasource.url=" + url,
                        "spring.datasource.username=" + username,
                        "spring.datasource.password=" + password)
                .run();
    }

    private SqlSmokeRepository sqlRepository(DataSource dataSource) {
        AppContext context = AppContext.builder()
                .withPackage(getClass().getPackageName())
                .withSingletonClass("one.xis.sql.DataSourceFactory")
                .withSingletonClass("one.xis.sql.DataSourceConfiguration")
                .withSingletonClass("one.xis.sql.SqlConnectionProvider")
                .withSingletonClass("one.xis.sql.TransactionManager")
                .withSingletonClass("one.xis.sql.SQLRepositoryProxyFactory")
                .withSingleton(dataSource)
                .build();
        return context.getSingleton(SqlSmokeRepository.class);
    }

    private MongoSmokeRepository mongoRepository(MongoDatabase database) {
        AppContext context = AppContext.builder()
                .withPackage(getClass().getPackageName())
                .withSingletonClass("one.xis.mongodb.MongoRepositoryProxyFactory")
                .withSingleton(database)
                .build();
        return context.getSingleton(MongoSmokeRepository.class);
    }

    @SpringBootConfiguration
    @EnableAutoConfiguration
    static class SqlSpringApplication {
    }

    @SpringBootConfiguration
    @EnableAutoConfiguration
    static class MongoSpringApplication {
        @Bean
        MongoClient mongoClient(Environment environment) {
            return MongoClients.create(environment.getRequiredProperty("spring.data.mongodb.uri"));
        }
    }
}
