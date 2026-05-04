package test.widget;

import one.xis.context.IntegrationTestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class FrontletShortTagTest {

    private IntegrationTestContext testContext;

    @BeforeEach
    void init() {
        testContext = IntegrationTestContext.builder()
                .withSingleton(SimpleFrontlet.class)
                .withSingleton(WidgetShortTagPage.class)
                .build();
    }

    @Test
    void frontletShortTagLoadsDefaultFrontlet() {
        testContext.getSingleton(WidgetShortTagPage.class).setWidgetId("SimpleFrontlet");
        var client = testContext.openPage(WidgetShortTagPage.class);

        assertThat(client.getDocument().getElementById("greeting").getInnerText()).isEqualTo("Huhu !");
    }
}
