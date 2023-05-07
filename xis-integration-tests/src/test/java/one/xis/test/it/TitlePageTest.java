package one.xis.test.it;

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
        testContext.openPage("/title.html");
        assertThat(testContext.getDocument().getElementByTagName("title").innerText).isEqualTo("Hello !");
    }
}
