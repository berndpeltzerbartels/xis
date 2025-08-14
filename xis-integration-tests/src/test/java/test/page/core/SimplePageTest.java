package test.page.core;

import one.xis.context.IntegrationTestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SimplePageTest {

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
        assertThat(result.getDocument().getElementByTagName("title").getInnerText()).isEqualTo("Hello ! I am the title");
    }
}
