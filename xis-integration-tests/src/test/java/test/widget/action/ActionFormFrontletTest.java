package test.widget.action;

import one.xis.context.IntegrationTestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ActionFormFrontletTest {

    private IntegrationTestContext context;

    @BeforeEach
    void setUp() {
        context = IntegrationTestContext.builder()
                .withSingleton(ActionFormPage.class)
                .withSingleton(ActionFormFrontlet.class)
                .withSingleton(AnotherFrontlet.class)
                .withSingleton(AnotherPage.class)
                .build();
    }

    @Test
    void stayHereAction() {
        var client = context.openPage(ActionFormPage.class);
        client.getDocument().getElementById("radio-1").click();
        client.getDocument().getElementById("submitButton").click();
        assertThat(client.getDocument().getElementById("submitButton")).isNotNull();
    }

    @Test
    void anotherWidgetAction() {
        var client = context.openPage(ActionFormPage.class);
        client.getDocument().getElementById("radio-2").click();
        client.getDocument().getElementById("submitButton").click();
        assertThat(client.getDocument().getTextContent()).contains("Another Frontlet Content");
    }

    @Test
    void anotherPageAction() {
        var client = context.openPage(ActionFormPage.class);
        client.getDocument().getElementById("radio-3").click();
        client.getDocument().getElementById("submitButton").click();
        assertThat(client.getDocument().getTextContent()).contains("Another Page Content");
    }
}
