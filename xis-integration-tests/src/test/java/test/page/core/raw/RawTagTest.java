package test.page.core.raw;

import one.xis.context.IntegrationTestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RawTagTest {

    private IntegrationTestContext testContext;

    @BeforeEach
    void init() {
        testContext = IntegrationTestContext.builder()
                .withSingleton(RawTagPage.class)
                .build();
    }

    @Test
    void insertsRawHtmlOrText() {
        var client = testContext.openPage("/rawTag.html");

        assertThat(client.getDocument().getElementById("raw-html").getInnerText()).isEqualTo("Trusted HTML");
        assertThat(client.getDocument().getElementById("raw-text")).isNull();
        assertThat(client.getDocument().getElementById("text-container").getInnerText())
                .contains("<em id=\"raw-text\">")
                .contains("Shown as text</em>");
    }
}
