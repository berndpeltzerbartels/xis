package test.frontlet.store.actionbutton;

import one.xis.context.IntegrationTestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class FrontletActionTest {
    private IntegrationTestContext context;

    @BeforeEach
    void init() {
        context = IntegrationTestContext.builder()
                .withSingleton(FrontletActionPage.class)
                .withSingleton(FrontletActionFrontlet.class)
                .build();
    }

    @Test
    void actionInFrontletUpdatesPageAndFrontlet() {
        var client = context.openPage("/frontletAction.html");

        // Initial values should be 5
        var counterOnPage = client.getDocument().getElementById("counter-value-page").getInnerText();
        var counterOnFrontlet = client.getDocument().getElementById("counter-value-frontlet").getInnerText();

        //  assertThat(counterOnPage).isEqualTo("5");
        assertThat(counterOnFrontlet).isEqualTo("0");

        // Click button in frontlet - this should trigger reactive state update
        client.getDocument().getElementById("increment-button").click();

        // Both page and frontlet should now show 6
        counterOnPage = client.getDocument().getElementById("counter-value-page").getInnerText();
        counterOnFrontlet = client.getDocument().getElementById("counter-value-frontlet").getInnerText();

        assertThat(counterOnPage).isEqualTo("1");
        assertThat(counterOnFrontlet).isEqualTo("1");
    }
}
