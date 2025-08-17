package test.widget.action;

import one.xis.context.IntegrationTestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ActionFormWidgetTest {

    private IntegrationTestContext context;

    @BeforeEach
    void setUp() {
        context = IntegrationTestContext.builder()
                .withSingleton(ActionFormPage.class)
                .withSingleton(ActionFormWidget.class)
                .withSingleton(AnotherWidget.class)
                .withSingleton(AnotherPage.class)
                .build();
    }

    @Test
    void stayHereAction() {
        var result = context.openPage(ActionFormPage.class);
        result.getDocument().getElementById("radio-1").click();
        result.getDocument().getElementById("submitButton").click();
        assertThat(result.getDocument().getElementById("submitButton")).isNotNull();
    }

    @Test
    void anotherWidgetAction() {
        var result = context.openPage(ActionFormPage.class);
        result.getDocument().getElementById("radio-2").click();
        result.getDocument().getElementById("submitButton").click();
        assertThat(result.getDocument().getTextContent()).contains("Another Widget Content");
    }

    @Test
    void anotherPageAction() {
        var result = context.openPage(ActionFormPage.class);
        result.getDocument().getElementById("radio-3").click();
        result.getDocument().getElementById("submitButton").click();
        assertThat(result.getDocument().getTextContent()).contains("Another Page Content");
    }
}