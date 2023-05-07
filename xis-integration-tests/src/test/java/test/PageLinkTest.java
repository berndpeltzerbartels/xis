package test;

import one.xis.context.IntegrationTestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PageLinkTest {

    private IntegrationTestContext testContext;

    @BeforeEach
    void init() {
        testContext = IntegrationTestContext.builder()
                .withSingleton(IndexPage.class)
                .withSingleton(TitlePage.class) // Link target
                .build();
    }

    @Test
    void test() {
        testContext.openPage("/index.html");
        testContext.getDocument().getElementById("title-link").onclick.accept(null);

        testContext.getDocument().getElementsByTagName("title");
    }
}
