package test.widget;

import one.xis.context.IntegrationTestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class ParameterFrontletTest {

    private ParameterWidgetService service;
    private IntegrationTestContext testContext;

    @BeforeEach
    void init() {
        service = mock(ParameterWidgetService.class);

        testContext = IntegrationTestContext.builder()
                .withSingleton(ParameterWidgetPage.class)
                .withSingleton(ParameterFrontlet.class)
                .withSingleton(service)
                .build();
    }

    @Test
    @DisplayName("Frontlet returns url-parameter for model and is displayed in widget")
    void urlParameter() {
        var client = testContext.openPage("/3/parameterWidgetPage.html?b=8");

        assertThat(client.getDocument().getElementById("urlParameter").getInnerText()).isEqualTo("8");

    }


    @Test
    @DisplayName("Frontlet returns path-variable for model and is displayed in widget")
    void pathVariable() {
        var client = testContext.openPage("/3/parameterWidgetPage.html");

        assertThat(client.getDocument().getElementById("pathVariable").getInnerText()).isEqualTo("3");
    }

    @Test
    @DisplayName("Action-method in widget is always called with the same parameters")
    void action() {
        var client = testContext.openPage("/3/parameterWidgetPage.html?b=8");
        client.getDocument().getElementByTagName("a").click();
        client.getDocument().getElementByTagName("a").click();
        client.getDocument().getElementByTagName("a").click();

        verify(service, times(3)).action(eq(3), eq(8), eq(42));

    }
}
