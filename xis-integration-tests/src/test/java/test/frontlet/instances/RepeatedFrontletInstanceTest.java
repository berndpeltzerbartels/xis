package test.frontlet.instances;

import one.xis.context.IntegrationTestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RepeatedFrontletInstanceTest {

    private IntegrationTestContext context;

    @BeforeEach
    void init() {
        context = IntegrationTestContext.builder()
                .withSingleton(RepeatedFrontletInstancePage.class)
                .withSingleton(RepeatedItemFrontlet.class)
                .build();
    }

    @Test
    void repeatedAnonymousContainersUseSeparateFrontletInstancesAndParameters() {
        var client = context.openPage(RepeatedFrontletInstancePage.class);

        assertThat(client.getDocument().querySelectorAll(".item-frontlet"))
                .extracting(element -> element.getInnerText())
                .containsExactly("1:1", "2:1");

        client.getDocument().getElementById("refresh-items").click();

        assertThat(client.getDocument().querySelectorAll(".item-frontlet"))
                .extracting(element -> element.getInnerText())
                .containsExactly("1:2", "2:2");
    }
}
