package test.page.core;

import one.xis.context.IntegrationTestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class FrontletButtonTest {

    private IntegrationTestContext testContext;

    @BeforeEach
    void init() {
        testContext = IntegrationTestContext.builder()
                .withSingleton(FrontletButtonPage.class)
                .withSingleton(FrontletButton1.class)
                .withSingleton(FrontletButton2.class)
                .build();
    }

    @Test
    @DisplayName("Click button to load frontlet, then click button to load another frontlet")
    void test() {
        var pageController = testContext.getSingleton(FrontletButtonPage.class);
        var frontlet1 = testContext.getSingleton(FrontletButton1.class);
        var frontlet2 = testContext.getSingleton(FrontletButton2.class);

        var client = testContext.openPage("/frontletButtonPage.html");
        assertThat(client.getDocument().getElementByTagName("title").getInnerText()).isEqualTo("Frontlet Button Test");
        assertThat(pageController.getInvocations()).isEqualTo(1);

        // Click button to load frontlet1
        client.getDocument().getElementById("loadFrontlet1").click();
        assertThat(frontlet1.getInvocations()).isEqualTo(1);
        assertThat(client.getDocument().getElementById("frontletContent").getInnerText()).contains("Frontlet 1 Content");

        // Click button to load frontlet2
        client.getDocument().getElementById("loadFrontlet2").click();
        assertThat(frontlet2.getInvocations()).isEqualTo(1);
        assertThat(client.getDocument().getElementById("frontletContent").getInnerText()).contains("Frontlet 2 Content");
    }
}
