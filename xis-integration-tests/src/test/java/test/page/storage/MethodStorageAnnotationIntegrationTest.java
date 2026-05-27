package test.page.storage;

import one.xis.context.IntegrationTestContext;
import one.xis.server.ClientRequest;
import one.xis.server.FrontendService;
import one.xis.server.RequestType;
import org.junit.jupiter.api.Test;

import java.time.ZoneId;

import static org.assertj.core.api.Assertions.assertThat;

class MethodStorageAnnotationIntegrationTest {

    @Test
    void storageMethodAnnotationsProvideSimpleValuesOnPageLoad() {
        var context = IntegrationTestContext.builder()
                .withSingleton(MethodStorageAnnotationPage.class)
                .build();

        var response = context.getSingleton(FrontendService.class).processModelDataRequest(pageRequest());

        assertThat(response.getLocalStorageData()).containsEntry("localSimple", "local-start");
        assertThat(response.getSessionStorageData()).containsEntry("sessionSimple", "session-start");
        assertThat(response.getClientStateData()).containsEntry("clientSimple", "client-start");
        assertThat((MethodStorageData) response.getLocalStorageData().get("localComplex"))
                .extracting(MethodStorageData::getKept, MethodStorageData::getRemoved)
                .containsExactly("local-kept", "local-removed");
    }

    @Test
    void storageMethodAnnotationsReturnNullAsRemovalSignal() {
        var context = IntegrationTestContext.builder()
                .withSingleton(MethodStorageAnnotationPage.class)
                .build();

        var response = context.getSingleton(FrontendService.class).processActionRequest(actionRequest("clear-all"));

        assertThat(response.getLocalStorageData()).containsEntry("localSimple", null);
        assertThat(response.getSessionStorageData()).containsEntry("sessionSimple", null);
        assertThat(response.getClientStateData()).containsEntry("clientSimple", null);
    }

    @Test
    void browserRemovesLocalAndSessionStorageWhenMethodReturnsNull() {
        var context = IntegrationTestContext.builder()
                .withSingleton(MethodStorageAnnotationPage.class)
                .build();

        var client = context.openPage(MethodStorageAnnotationPage.class);
        assertThat(client.getLocalStorage().getItem("localSimple")).contains("local-start");
        assertThat(client.getSessionStorage().getItem("sessionSimple")).contains("session-start");

        client.getDocument().getElementById("clear-all").click();

        assertThat(client.getLocalStorage().getItem("localSimple")).isNull();
        assertThat(client.getSessionStorage().getItem("sessionSimple")).isNull();
    }

    @Test
    void storageMethodParametersCanClearSingleFieldsInComplexValues() {
        var context = IntegrationTestContext.builder()
                .withSingleton(MethodStorageAnnotationPage.class)
                .build();

        var response = context.getSingleton(FrontendService.class).processActionRequest(actionRequestWithComplexStorage("clear-complex-fields"));

        assertComplexValue(response.getLocalStorageData().get("localComplex"), "local-kept");
        assertComplexValue(response.getSessionStorageData().get("sessionComplex"), "session-kept");
        assertComplexValue(response.getClientStateData().get("clientComplex"), "client-kept");
    }

    @Test
    void browserStoresComplexValuesWithNullFieldsFromStorageMethodParameters() {
        var context = IntegrationTestContext.builder()
                .withSingleton(MethodStorageAnnotationPage.class)
                .build();

        var client = context.openPage(MethodStorageAnnotationPage.class);

        client.getDocument().getElementById("clear-complex-fields").click();

        assertThat(client.getLocalStorage().getItem("localComplex"))
                .contains("\"kept\":\"local-kept\"")
                .contains("\"removed\":null");
        assertThat(client.getSessionStorage().getItem("sessionComplex"))
                .contains("\"kept\":\"session-kept\"")
                .contains("\"removed\":null");
    }

    private ClientRequest pageRequest() {
        var request = new ClientRequest();
        request.setClientId("method-storage-test-client");
        request.setPageId("/method-storage-annotation.html");
        request.setPageUrl("/method-storage-annotation.html");
        request.setType(RequestType.page);
        request.setZoneId(ZoneId.systemDefault().getId());
        return request;
    }

    private ClientRequest actionRequest(String action) {
        var request = pageRequest();
        request.setAction(action);
        return request;
    }

    private ClientRequest actionRequestWithComplexStorage(String action) {
        var request = actionRequest(action);
        request.getLocalStorageData().put("localComplex", "{\"kept\":\"local-kept\",\"removed\":\"local-removed\"}");
        request.getSessionStorageData().put("sessionComplex", "{\"kept\":\"session-kept\",\"removed\":\"session-removed\"}");
        request.getClientStateData().put("clientComplex", "{\"kept\":\"client-kept\",\"removed\":\"client-removed\"}");
        return request;
    }

    private void assertComplexValue(Object value, String kept) {
        assertThat(value).isInstanceOf(MethodStorageData.class);
        var storageData = (MethodStorageData) value;
        assertThat(storageData.getKept()).isEqualTo(kept);
        assertThat(storageData.getRemoved()).isNull();
    }
}
