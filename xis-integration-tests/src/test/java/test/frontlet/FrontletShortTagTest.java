package test.frontlet;

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
                .withSingleton(FrontletShortTagPage.class)
                .build();
    }

    @Test
    void frontletShortTagLoadsDefaultFrontlet() {
        testContext.getSingleton(FrontletShortTagPage.class).setFrontletId("SimpleFrontlet");
        var client = testContext.openPage(FrontletShortTagPage.class);

        assertThat(client.getDocument().getElementById("greeting").getInnerText()).isEqualTo("Huhu !");
    }
}
