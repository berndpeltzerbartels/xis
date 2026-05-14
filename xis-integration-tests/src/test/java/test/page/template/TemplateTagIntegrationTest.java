package test.page.template;

import one.xis.context.IntegrationTestContext;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TemplateTagIntegrationTest {

    @Test
    void actionAndButtonTagsTriggerActionsAndNavigation() {
        var context = IntegrationTestContext.builder()
                .withSingleton(TemplateActionTagsPage.class)
                .withSingleton(TemplateTargetPage.class)
                .build();
        var client = context.openPage(TemplateActionTagsPage.class);

        client.getDocument().getElementById("action-tag").click();
        assertThat(client.getDocument().getElementById("action-result").getInnerText())
                .isEqualTo("from-action-tag");

        client.getDocument().getElementById("action-button-tag").click();
        assertThat(client.getDocument().getElementById("button-clicks").getInnerText())
                .isEqualTo("1");

        client.getDocument().getElementById("page-button-tag").click();
        assertThat(client.getDocument().getElementById("target-title").getInnerText())
                .isEqualTo("Template Target");
    }

    @Test
    void messageTagShowsFieldValidationMessage() {
        var context = IntegrationTestContext.builder()
                .withSingleton(TemplateMessageTagPage.class)
                .build();
        var client = context.openPage(TemplateMessageTagPage.class);

        client.getDocument().getElementById("save").click();

        assertThat(client.getDocument().getElementById("name-message").getInnerText())
                .isNotBlank();
    }

    @Test
    void storageBindingTagReadsDeclaredStore() {
        var context = IntegrationTestContext.builder()
                .withSingleton(TemplateStorageBindingTagPage.class)
                .build();
        context.getLocalStorage().setItem("theme", "{\"value\":\"dark\"}");

        var client = context.openPage(TemplateStorageBindingTagPage.class);

        assertThat(client.getDocument().getElementById("storage-binding").getInnerText())
                .contains("Theme: dark");
    }
}
