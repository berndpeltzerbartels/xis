package test;

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
    @DisplayName("xis:a is replaced by html-a-element")
    void test() {
        testContext.openPage(AnchorReplaceTestPage.class);

        var pageLink = testContext.getDocument().getElementById("page-link");
        var widgetLink = testContext.getDocument().getElementById("widget-link");

        assertThat(pageLink._handler).isNotNull();
        assertThat(widgetLink._handler).isNotNull();

        assertThat(pageLink.getChildElements()).isEmpty();
        assertThat(widgetLink.getChildElements()).isEmpty();

        assertThat(pageLink.parentNode.localName).isEqualTo("body");
        assertThat(widgetLink.parentNode.localName).isEqualTo("body");
    }
}
