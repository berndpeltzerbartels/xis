package one.xis.test.it;

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
        testContext.openPage("/titleAndHeadline.html");
        assertThat(testContext.getDocument().getElementByTagName("title").innerText).isEqualTo("title");
        assertThat(testContext.getDocument().getElementByTagName("h1").innerText).isEqualTo("headline");
    }
}
