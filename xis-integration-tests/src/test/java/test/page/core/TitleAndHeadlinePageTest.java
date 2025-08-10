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
        var result = testContext.openPage("/titleAndHeadline.html");
        assertThat(result.getDocument().getElementByTagName("title").getInnerText()).isEqualTo("title");
        assertThat(result.getDocument().getElementByTagName("h1").getInnerText()).isEqualTo("headline");
    }
}
