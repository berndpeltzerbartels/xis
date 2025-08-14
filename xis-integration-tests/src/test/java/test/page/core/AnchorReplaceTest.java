package test.page.core;

import one.xis.context.IntegrationTestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AnchorReplaceTest {


    private IntegrationTestContext testContext;

    @BeforeEach
    void init() {
        testContext = IntegrationTestContext.builder()
                .withSingleton(AnchorReplaceTestPage.class)
                .build();
    }

    @Test
    @DisplayName("xis:a is getting replaced by html-a-element")
    void test() {
        var result = testContext.openPage(AnchorReplaceTestPage.class);

        var pageLink = result.getDocument().getElementById("page-link");
        var widgetLink = result.getDocument().getElementById("widget-link");

        assertThat(pageLink.getChildElements()).isEmpty();
        assertThat(widgetLink.getChildElements()).isEmpty();

        assertThat(pageLink.getParentNode().getLocalName()).isEqualTo("body");
        assertThat(widgetLink.getParentNode().getLocalName()).isEqualTo("body");
    }
}
