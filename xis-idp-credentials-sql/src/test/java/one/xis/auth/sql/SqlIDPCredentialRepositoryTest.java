package one.xis.auth.sql;

import one.xis.auth.IDPCredentialService;
import one.xis.auth.Password4jIDPCredentialService;
import one.xis.context.AppContext;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SqlIDPCredentialRepositoryTest {

    @Test
    void storesHashedIDPCredentialsInSql() {
        var dataSource = new JdbcDataSource();
        dataSource.setURL("jdbc:h2:mem:idp-credentials-test;DB_CLOSE_DELAY=-1");

        AppContext context = AppContext.builder()
                .withXIS()
                .withPackage("one.xis.sql")
                .withPackage("one.xis.auth.sql")
                .withSingletonClass(Password4jIDPCredentialService.class)
                .withSingleton(dataSource)
                .build();

        IDPCredentialService credentialService = context.getSingleton(Password4jIDPCredentialService.class);

        credentialService.setUserPassword("alice", "secret");
        credentialService.setClientSecret("orders-app", "orders-secret");

        assertThat(credentialService.validateUserCredentials("alice", "secret")).isTrue();
        assertThat(credentialService.validateUserCredentials("alice", "wrong")).isFalse();
        assertThat(credentialService.validateClientSecret("orders-app", "orders-secret")).isTrue();
        assertThat(credentialService.validateClientSecret("orders-app", "wrong")).isFalse();
        assertThat(context.getSingleton(SqlIDPCredentialRepository.class).findUserById("alice"))
                .hasValueSatisfying(credentials -> {
                    assertThat(credentials.getPasswordHash()).startsWith("$argon2id$");
                    assertThat(credentials.getPasswordHash()).doesNotContain("secret");
                });
        assertThat(context.getSingleton(SqlIDPCredentialRepository.class).findClientById("orders-app"))
                .hasValueSatisfying(credentials -> {
                    assertThat(credentials.getClientSecretHash()).startsWith("$argon2id$");
                    assertThat(credentials.getClientSecretHash()).doesNotContain("orders-secret");
                });
    }
}
