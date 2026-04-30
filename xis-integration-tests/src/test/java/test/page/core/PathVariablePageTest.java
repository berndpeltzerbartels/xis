package test.page.core;

import one.xis.context.IntegrationTestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PathVariablePageTest {

    private IntegrationTestContext testContext;

    @BeforeEach
    void init() {
        testContext = IntegrationTestContext.builder()
                .withSingleton(PathVariablePage.class)
                .build();
    }

    @Test
    void test() {
        var client = testContext.openPage("/url-parameter/bla/123.html");
        assertThat(client.getDocument().getElementById("client").getInnerText()).isEqualTo("bla123");
    }
}
