package test.frontlet.instances;

import one.xis.context.IntegrationTestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RepeatedActionFrontletInstanceTest {

    private IntegrationTestContext context;

    @BeforeEach
    void init() {
        context = IntegrationTestContext.builder()
                .withSingleton(RepeatedActionFrontletInstancePage.class)
                .withSingleton(RepeatedActionItemFrontlet.class)
                .build();
    }

    @Test
    void repeatedAnonymousContainersInsideActionButtonsUseSeparateFrontletInstancesAndParameters() {
        var client = context.openPage(RepeatedActionFrontletInstancePage.class);

        assertThat(client.getDocument().querySelectorAll(".item-frontlet"))
                .extracting(element -> element.getInnerText())
                .containsExactly("1:1", "2:1");

        client.getDocument().getElementById("refresh-action-items").click();

        assertThat(client.getDocument().querySelectorAll(".item-frontlet"))
                .extracting(element -> element.getInnerText())
                .containsExactly("1:2", "2:2");
    }
}
