package test.page.i18n;

import one.xis.context.IntegrationTestContext;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class I18nMessagesIntegrationTest {

    @Test
    void exposesLoadedMessageBundleInTemplateExpressions() {
        var context = IntegrationTestContext.builder()
                .withSingleton(I18nMessagesPage.class)
                .build();

        var client = context.openPage("/i18n-messages.html");

        assertThat(client.getDocument().getElementByTagName("title").getInnerText())
                .isEqualTo("I18N Test Page");
        assertThat(client.getDocument().getElementById("simple").getInnerText())
                .isEqualTo("Hello from messages");
        assertThat(client.getDocument().getElementById("dotted").getInnerText())
                .isEqualTo("Dotted key");
    }

    @Test
    void messageEndpointUsesRequestLocale() {
        var context = IntegrationTestContext.builder()
                .withSingleton(I18nMessagesPage.class)
                .build();

        var response = context.invokeBackend("GET", "/xis/messages", Map.of("Accept-Language", "de-DE,de;q=0.9"));

        assertThat(response.status).isEqualTo(200);
        assertThat(response.responseText).contains("\"i18nLocalized\":\"Deutsches Bundle\"");
        assertThat(response.responseText).contains("\"i18nSimple\":\"Hello from messages\"");
    }
}
