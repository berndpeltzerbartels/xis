package test.page.frontend;

import one.xis.context.IntegrationTestContext;
import one.xis.gson.JsonMap;
import one.xis.server.ActionProcessing;
import one.xis.server.ClientRequest;
import one.xis.server.FrontendService;
import one.xis.server.RequestType;
import org.junit.jupiter.api.Test;

import java.time.ZoneId;

import static org.assertj.core.api.Assertions.assertThat;

class FrontendParameterIntegrationTest {

    @Test
    void frontendParameterAddsModelDataFormDataAndToastMessages() {
        var context = IntegrationTestContext.builder()
                .withPackage("test.page.frontend")
                .build();
        var client = context.openPage(FrontendParameterPage.class);
        var document = client.getDocument();

        assertThat(document.getInputElementById("value").getValue()).isEqualTo("initial");
        assertThat(document.getElementById("model-result").getInnerText()).isEqualTo("main-loaded");
        assertThat(document.getElementById("model-side-result").getInnerText()).isEqualTo("side-loaded");
        assertThat(document.getElementById("result").getInnerText()).isEmpty();

        document.getInputElementById("value").setValue("input");
        document.getElementById("save").click();

        assertThat(document.getElementById("result").getInnerText()).isEqualTo("saved-input");
        assertThat(document.getInputElementById("value").getValue()).isEqualTo("server-input");
        assertThat(document.querySelector("#xis-toast-container .xis-toast").getInnerText()).isEqualTo("Saved input");
        assertThat(document.querySelector("#xis-toast-container .xis-toast").getCssClasses()).contains("success");
    }

    @Test
    void frontendParameterContributesToServerActionResponse() {
        var context = IntegrationTestContext.builder()
                .withPackage("test.page.frontend")
                .build();

        var response = context.getSingleton(FrontendService.class).processActionRequest(actionRequest());

        assertThat(response.getActionProcessing()).isEqualTo(ActionProcessing.PAGE);
        assertThat(response.getData()).containsEntry("result", "saved-input");
        assertThat(response.getFormData()).containsKey("form");
        assertThat(response.getReturnedFormDataKeys()).contains("form");
        assertThat(response.getToastMessages()).extracting("message").containsExactly("Saved input");
    }

    @Test
    void frontendParameterContributesToServerModelDataResponse() {
        var context = IntegrationTestContext.builder()
                .withPackage("test.page.frontend")
                .build();

        var response = context.getSingleton(FrontendService.class).processModelDataRequest(modelDataRequest());

        assertThat(response.getData()).containsEntry("modelResult", "main-loaded");
        assertThat(response.getData()).containsEntry("modelSideResult", "side-loaded");
    }

    private ClientRequest actionRequest() {
        var request = new ClientRequest();
        request.setClientId("frontend-test-client");
        request.setPageId("/frontend-parameter.html");
        request.setPageUrl("/frontend-parameter.html");
        request.setType(RequestType.page);
        request.setZoneId(ZoneId.systemDefault().getId());
        request.setAction("save");
        request.setFormBinding("form");
        request.setFormData(JsonMap.of("form", "{\"value\":\"input\"}"));
        return request;
    }

    private ClientRequest modelDataRequest() {
        var request = new ClientRequest();
        request.setClientId("frontend-test-client");
        request.setPageId("/frontend-parameter.html");
        request.setPageUrl("/frontend-parameter.html");
        request.setType(RequestType.page);
        request.setZoneId(ZoneId.systemDefault().getId());
        return request;
    }
}
