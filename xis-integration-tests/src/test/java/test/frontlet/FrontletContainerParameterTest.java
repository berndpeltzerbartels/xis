package test.frontlet;

import one.xis.context.IntegrationTestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class FrontletContainerParameterTest {

    private FrontletContainerParameterService service;
    private IntegrationTestContext testContext;

    @BeforeEach
    void init() {
        service = mock(FrontletContainerParameterService.class);

        testContext = IntegrationTestContext.builder()
                .withSingleton(FrontletContainerParameterPage.class)
                .withSingleton(FrontletContainerParameterFrontlet.class)
                .withSingleton(SecondFrontletContainerParameterFrontlet.class)
                .withSingleton(service)
                .build();
    }

    @Test
    @DisplayName("Frontlet container with xis:parameter tags passes parameters to frontlet")
    void frontletContainerWithParameterTags() {
        var client = testContext.openPage("/frontletContainerParameterPage.html");

        assertThat(client.getDocument().getElementById("categoryId").getInnerText()).isEqualTo("electronics");
        assertThat(client.getDocument().getElementById("sortBy").getInnerText()).isEqualTo("price");
    }

    @Test
    @DisplayName("Frontlet container parameters are available in action methods")
    void frontletContainerParametersInAction() {
        var client = testContext.openPage("/frontletContainerParameterPage.html");
        client.getDocument().getElementByTagName("a").click();

        verify(service, times(1)).action(eq("electronics"), eq("price"));
    }

    @Test
    @DisplayName("Frontlet container parameters survive frontlet changes")
    void frontletContainerParametersSurviveFrontletChanges() {
        var client = testContext.openPage("/frontletContainerParameterPage.html");

        client.getDocument().getElementById("showSecond").click();

        assertThat(client.getDocument().getElementById("secondCategoryId").getInnerText()).isEqualTo("electronics");
    }
}
