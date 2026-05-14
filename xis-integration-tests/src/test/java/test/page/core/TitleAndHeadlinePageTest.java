package test.page.core;

import one.xis.context.IntegrationTestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TitleAndHeadlinePageTest {

    private IntegrationTestContext testContext;

    @BeforeEach
    void init() {
        testContext = IntegrationTestContext.builder()
                .withSingleton(TitleAndHeadlinePage.class)
                .build();
    }

    @Test
    void test() {
        var client = testContext.openPage("/titleAndHeadline.html");
        assertThat(client.getDocument().getElementByTagName("title").getInnerText()).isEqualTo("title");
        assertThat(client.getDocument().getElementByTagName("h1").getInnerText()).isEqualTo("headline");
    }
}
