package one.xis.auth.sql;

import one.xis.auth.LocalCredentialService;
import one.xis.auth.Password4jLocalCredentialService;
import one.xis.context.AppContext;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SqlLocalCredentialRepositoryTest {

    @Test
    void storesHashedCredentialsInSql() {
        var dataSource = new JdbcDataSource();
        dataSource.setURL("jdbc:h2:mem:local-credentials-test;DB_CLOSE_DELAY=-1");

        AppContext context = AppContext.builder()
                .withXIS()
                .withPackage("one.xis.sql")
                .withPackage("one.xis.auth")
                .withPackage("one.xis.auth.sql")
                .withSingleton(dataSource)
                .build();

        LocalCredentialService credentialService = context.getSingleton(Password4jLocalCredentialService.class);

        credentialService.setPassword("alice", "secret");

        assertThat(credentialService.validateCredentials("alice", "secret")).isTrue();
        assertThat(credentialService.validateCredentials("alice", "wrong")).isFalse();
        assertThat(context.getSingleton(SqlLocalCredentialRepository.class).findByUserId("alice"))
                .hasValueSatisfying(credentials -> {
                    assertThat(credentials.getPasswordHash()).startsWith("$argon2id$");
                    assertThat(credentials.getPasswordHash()).doesNotContain("secret");
                });
    }
}
