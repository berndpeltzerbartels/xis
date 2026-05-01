package test.widget;

import one.xis.context.IntegrationTestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class WidgetContainerParameterTest {

    private WidgetContainerParameterService service;
    private IntegrationTestContext testContext;

    @BeforeEach
    void init() {
        service = mock(WidgetContainerParameterService.class);

        testContext = IntegrationTestContext.builder()
                .withSingleton(WidgetContainerParameterPage.class)
                .withSingleton(WidgetContainerParameterFrontlet.class)
                .withSingleton(service)
                .build();
    }

    @Test
    @DisplayName("Frontlet container with xis:parameter tags passes parameters to widget")
    void widgetContainerWithParameterTags() {
        var client = testContext.openPage("/widgetContainerParameterPage.html");

        assertThat(client.getDocument().getElementById("categoryId").getInnerText()).isEqualTo("electronics");
        assertThat(client.getDocument().getElementById("sortBy").getInnerText()).isEqualTo("price");
    }

    @Test
    @DisplayName("Frontlet container parameters are available in action methods")
    void widgetContainerParametersInAction() {
        var client = testContext.openPage("/widgetContainerParameterPage.html");
        client.getDocument().getElementByTagName("a").click();

        verify(service, times(1)).action(eq("electronics"), eq("price"));
    }
}
