package test.page.core;

import one.xis.context.IntegrationTestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LinkWithParamTest {

    private IntegrationTestContext context;

    @BeforeEach
    void init() {
        context = IntegrationTestContext.builder()
                .withSingleton(LinkWithParamPage1.class)
                .withSingleton(LinkWithParamPage2.class)
                .build();
    }

    @Test
    void clickLink() {
        var result = context.openPage("/linkWithParamPage1.html"); // open link 1 first time
        result.getDocument().getElementById("page-link").click();

        assertThat(result.getDocument().getElementByTagName("title").getInnerText()).isEqualTo("The title is 'bla'");
        assertThat(result.getDocument().getElementByTagName("h1").getInnerText()).isEqualTo("The title is 'bla'");

    }
}
