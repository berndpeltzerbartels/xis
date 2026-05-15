package test.page.modal;

import one.xis.context.IntegrationTestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ModalIntegrationTest {

    private IntegrationTestContext context;
    private ModalIntegrationService service;

    @BeforeEach
    void setUp() {
        service = new ModalIntegrationService();
        context = IntegrationTestContext.builder()
                .withSingleton(service)
                .withSingleton(ModalIntegrationPage.class)
                .withSingleton(EditModal.class)
                .withSingleton(FormOnlyModal.class)
                .build();
    }

    @Test
    void modalButtonLoadsModalControllerAndModalActionReloadsParent() {
        var client = context.openPage("/modal-integration.html");

        client.getDocument().getElementById("open-modal-button").click();

        assertThat(service.modalLoadCount()).isEqualTo(1);
        assertThat(service.modalSource()).isEqualTo("button");

        client.getDocument().getInputElementById("modal-value").setValue("saved from modal");
        client.getDocument().getElementById("modal-save").click();

        assertThat(service.savedValue()).isEqualTo("saved from modal");
        assertThat(client.getDocument().getElementById("page-version").getInnerText()).isEqualTo("1");
        assertThat(client.getDocument().getElementById("saved-value").getInnerText()).isEqualTo("saved from modal");
        assertThat(client.getDocument().getInputElementById("parent-form-value").getValue()).isEqualTo("saved from modal");
    }

    @Test
    void modalLinkLoadsModalControllerWithoutFollowingHref() {
        var client = context.openPage("/modal-integration.html");
        var historySize = client.getDocument().getDefaultView().getHistory().getEntries().size();
        var href = client.getDocument().getLocation().getHref();

        client.getDocument().getElementById("open-modal-link").click();

        assertThat(service.modalLoadCount()).isEqualTo(1);
        assertThat(service.modalSource()).isEqualTo("link");
        assertThat(client.getDocument().getLocation().getPathname()).isEqualTo("/modal-integration.html");
        assertThat(client.getDocument().getLocation().getHref()).isEqualTo(href);
        assertThat(client.getDocument().getDefaultView().getHistory().getEntries()).hasSize(historySize);
    }

    @Test
    void modalResponseLoadsModalController() {
        var client = context.openPage("/modal-integration.html");

        client.getDocument().getElementById("open-modal-action").click();

        assertThat(service.modalLoadCount()).isEqualTo(1);
        assertThat(service.modalSource()).isEqualTo("action");
    }

    @Test
    void modalResponseFromActionKeepsModalParameters() {
        var client = context.openPage("/modal-integration.html");

        client.getDocument().getElementById("open-modal-action-parameter").click();

        assertThat(service.modalLoadCount()).isEqualTo(1);
        assertThat(service.modalSource()).isEqualTo("action parameter");
        assertThat(client.getDocument().getInputElementById("modal-value").getValue()).isEqualTo("action parameter");
    }

    @Test
    void modalResponseFromActionKeepsModalParametersForFormDataOnlyModal() {
        var client = context.openPage("/modal-integration.html");

        client.getDocument().getElementById("open-form-only-modal-action-parameter").click();

        assertThat(client.getDocument().getInputElementById("form-only-modal-value").getValue()).isEqualTo("form only action parameter");
    }

    @Test
    void modalUrlQueryIsAvailableAsParameter() {
        var client = context.openPage("/modal-integration.html");

        client.getDocument().getElementById("open-modal-query").click();

        assertThat(service.modalLoadCount()).isEqualTo(1);
        assertThat(service.modalSource()).isEqualTo("query parameter");
        assertThat(client.getDocument().getInputElementById("modal-value").getValue()).isEqualTo("query parameter");
    }
}
