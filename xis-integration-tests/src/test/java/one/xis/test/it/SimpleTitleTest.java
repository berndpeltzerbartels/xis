package one.xis.test.it;

import one.xis.context.IntegrationTestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SimpleTitleTest {

    private IntegrationTestContext testContext;

    @BeforeEach
    void init() {
        testContext = IntegrationTestContext.builder()
                .withSingleton(SimpleTitle.class)
                .build();
    }

    @Test
    void test() {
        testContext.openPage("/simpleTitle.html");
        assertThat(testContext.getDocument().getElementByTagName("title").innerText).isEqualTo("Hello !");
    }
}
