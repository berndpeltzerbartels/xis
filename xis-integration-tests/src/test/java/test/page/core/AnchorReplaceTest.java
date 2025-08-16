package test.page.core;

import one.xis.context.IntegrationTestContext;
import one.xis.test.dom.ElementImpl;
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

        var pageLink = (ElementImpl) result.getDocument().getElementById("page-link");
        var widgetLink = ((ElementImpl) result.getDocument().getElementById("widget-link"));

        assertThat(pageLink.getChildElements()).isEmpty();
        assertThat(widgetLink.getChildElements()).isEmpty();

        assertThat(((ElementImpl) pageLink.getParentNode()).getLocalName()).isEqualTo("body");
        assertThat(((ElementImpl) widgetLink.getParentNode()).getLocalName()).isEqualTo("body");
    }
}
