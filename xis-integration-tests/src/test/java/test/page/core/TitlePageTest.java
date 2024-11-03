package test.page.core;

import one.xis.context.IntegrationTestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TitlePageTest {

    private IntegrationTestContext testContext;

    @BeforeEach
    void init() {
        testContext = IntegrationTestContext.builder()
                .withSingleton(TitlePage.class)
                .build();
    }

    @Test
    void test() {
        var result = testContext.openPage("/title.html");
        assertThat(result.getDocument().getElementByTagName("title").innerText).isEqualTo("Hello ! I am the title");
    }
}
