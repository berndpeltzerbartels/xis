package test.widget;

import one.xis.context.IntegrationTestContext;
import one.xis.test.js.Event;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class ParameterWidgetTest {

    private ParameterWidgetService service;
    private IntegrationTestContext testContext;

    @BeforeEach
    void init() {
        service = mock(ParameterWidgetService.class);

        testContext = IntegrationTestContext.builder()
                .withSingleton(ParameterWidgetPage.class)
                .withSingleton(ParameterWidget.class)
                .withSingleton(service)
                .build();
    }

    @Test
    @DisplayName("Widget returns url-parameter for model and is displayed in widget")
    void urlParameter() {
        var result = testContext.openPage("/3/parameterWidgetPage.html?b=8");

        assertThat(result.getDocument().getElementById("urlParameter").innerText).isEqualTo("8");

    }


    @Test
    @DisplayName("Widget returns path-variable for model and is displayed in widget")
    void pathVariable() {
        var result = testContext.openPage("/3/parameterWidgetPage.html");

        assertThat(result.getDocument().getElementById("pathVariable").innerText).isEqualTo("3");
    }

    @Test
    @DisplayName("Action-method in widget is always called with the same parameters")
    void action() {
        var result = testContext.openPage("/3/parameterWidgetPage.html?b=8");
        result.getDocument().getElementByTagName("a").onclick.accept(new Event());
        result.getDocument().getElementByTagName("a").onclick.accept(new Event());
        result.getDocument().getElementByTagName("a").onclick.accept(new Event());

        verify(service, times(3)).action(eq(3), eq(8), eq(42));

    }
}
