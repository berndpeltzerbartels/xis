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
        assertThat(client.getDocument().getElementById("result").getInnerText()).isEqualTo("bla123");
    }

    @Test
    void actionLinkReceivesPathVariables() {
        var client = testContext.openPage("/url-parameter/bla/123.html");

        client.getDocument().getElementById("delete-link").click();

        assertThat(client.getDocument().getElementById("action-result").getInnerText()).isEqualTo("bla123");
    }

    @Test
    void actionLinkReceivesQueryParameters() {
        var client = testContext.openPage("/url-parameter/bla/123.html?filter=active&page=7");

        client.getDocument().getElementById("delete-with-query-link").click();

        assertThat(client.getDocument().getElementById("action-result").getInnerText()).isEqualTo("active7");
    }
}
