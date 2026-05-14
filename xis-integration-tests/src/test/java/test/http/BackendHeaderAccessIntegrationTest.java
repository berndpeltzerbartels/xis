package test.http;

import one.xis.context.IntegrationTestContext;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BackendHeaderAccessIntegrationTest {

    @Test
    void exposesCustomResponseHeaders() {
        var context = IntegrationTestContext.builder()
                .withBasePackageClass(HeaderTestController.class)
                .build();

        var response = context.invokeBackend("GET", "/test-header");

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.getResponseHeader("X-Test-Header")).isEqualTo("header-value");
        assertThat(response.getResponseText()).isEqualTo("ok");
    }

    @Test
    void exposesRedirectLocationHeader() {
        var context = IntegrationTestContext.builder()
                .withBasePackageClass(HeaderTestController.class)
                .build();

        var response = context.invokeBackend("GET", "/test-redirect");

        assertThat(response.getStatus()).isEqualTo(302);
        assertThat(response.getResponseHeader("Location")).isEqualTo("/target-page.html");
    }
}
