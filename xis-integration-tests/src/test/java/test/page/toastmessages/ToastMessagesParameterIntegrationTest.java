package test.page.toastmessages;

import one.xis.context.IntegrationTestContext;
import one.xis.gson.JsonMap;
import one.xis.server.ActionProcessing;
import one.xis.server.ClientRequest;
import one.xis.server.FrontendService;
import one.xis.server.RequestType;
import org.junit.jupiter.api.Test;

import java.time.ZoneId;

import static org.assertj.core.api.Assertions.assertThat;

class ToastMessagesParameterIntegrationTest {

    @Test
    void toastMessagesParameterAddsToastMessages() {
        var context = IntegrationTestContext.builder()
                .withPackage("test.page.toastmessages")
                .build();
        var client = context.openPage(ToastMessagesParameterPage.class);
        var document = client.getDocument();

        assertThat(document.getInputElementById("value").getValue()).isEqualTo("initial");
        assertThat(document.getElementById("model-result").getInnerText()).isEqualTo("main-loaded");

        document.getInputElementById("value").setValue("input");
        document.getElementById("save").click();

        assertThat(document.querySelector("#xis-toast-container .xis-toast").getInnerText()).isEqualTo("Saved input");
        assertThat(document.querySelector("#xis-toast-container .xis-toast").getCssClasses()).contains("success");
    }

    @Test
    void toastMessagesParameterContributesToServerActionResponse() {
        var context = IntegrationTestContext.builder()
                .withPackage("test.page.toastmessages")
                .build();

        var response = context.getSingleton(FrontendService.class).processActionRequest(actionRequest());

        assertThat(response.getActionProcessing()).isEqualTo(ActionProcessing.PAGE);
        assertThat(response.getData()).doesNotContainKey("result");
        assertThat(response.getFormData()).containsKey("form");
        assertThat(response.getReturnedFormDataKeys()).isEmpty();
        assertThat(response.getToastMessages()).extracting("message").containsExactly("Saved input");
    }

    @Test
    void modelDataResponseDoesNotReceiveDynamicToastParameterData() {
        var context = IntegrationTestContext.builder()
                .withPackage("test.page.toastmessages")
                .build();

        var response = context.getSingleton(FrontendService.class).processModelDataRequest(modelDataRequest());

        assertThat(response.getData()).containsEntry("modelResult", "main-loaded");
        assertThat(response.getData()).doesNotContainKey("modelSideResult");
    }

    private ClientRequest actionRequest() {
        var request = new ClientRequest();
        request.setClientId("toast-messages-test-client");
        request.setPageId("/toast-messages-parameter.html");
        request.setPageUrl("/toast-messages-parameter.html");
        request.setType(RequestType.page);
        request.setZoneId(ZoneId.systemDefault().getId());
        request.setAction("save");
        request.setFormBinding("form");
        request.setFormData(JsonMap.of("form", "{\"value\":\"input\"}"));
        return request;
    }

    private ClientRequest modelDataRequest() {
        var request = new ClientRequest();
        request.setClientId("toast-messages-test-client");
        request.setPageId("/toast-messages-parameter.html");
        request.setPageUrl("/toast-messages-parameter.html");
        request.setType(RequestType.page);
        request.setZoneId(ZoneId.systemDefault().getId());
        return request;
    }
}
