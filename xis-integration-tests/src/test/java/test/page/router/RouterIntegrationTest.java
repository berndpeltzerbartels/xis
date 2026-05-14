package test.page.router;

import one.xis.context.IntegrationTestContext;
import one.xis.UserContextImpl;
import one.xis.server.ClientRequest;
import one.xis.server.FrontendService;
import one.xis.server.RequestType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.ZoneId;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RouterIntegrationTest {

    private IntegrationTestContext context;

    @BeforeEach
    void setUp() {
        context = IntegrationTestContext.builder()
                .withSingleton(RouterIntegrationRouter.class)
                .withSingleton(RouterTargetPage.class)
                .build();
    }

    @Test
    void routerCanNavigateByStringAndPassPathAndQueryParameters() {
        var response = processRouterRequest("/router/string/*.html", "/router/string/42.html?tab=details");

        assertThat(response.getNextURL()).isEqualTo("/router-target/42.html?tab=details");
        assertThat(response.getData().get("targetId")).isEqualTo("42");
        assertThat(response.getData().get("targetTab")).isEqualTo("details");
    }

    @Test
    void routerCanNavigateByPageResponseAndPassPathAndQueryParameters() {
        var response = processRouterRequest("/router/page-response/*.html", "/router/page-response/84.html?tab=history");

        assertThat(response.getNextURL()).isEqualTo("/router-target/84.html?tab=history");
        assertThat(response.getData().get("targetId")).isEqualTo("84");
        assertThat(response.getData().get("targetTab")).isEqualTo("history");
    }

    @Test
    void routeMethodsAreRejectedOutsideRouterControllers() {
        assertThatThrownBy(() -> IntegrationTestContext.builder()
                .withSingleton(InvalidRoutePage.class)
                .build())
                .isInstanceOf(RuntimeException.class)
                .hasRootCauseMessage("@Route methods are only supported on @Router controllers: java.lang.String test.page.router.InvalidRoutePage.invalidRoute()");
    }

    private one.xis.server.ServerResponse processRouterRequest(String pageId, String pageUrl) {
        UserContextImpl.getInstance().setZoneId(ZoneId.systemDefault());
        var request = new ClientRequest();
        request.setClientId("router-test-client");
        request.setPageId(pageId);
        request.setPageUrl(pageUrl);
        request.setType(RequestType.page);
        request.setZoneId(ZoneId.systemDefault().getId());
        return context.getAppContext().getSingleton(FrontendService.class).processModelDataRequest(request);
    }
}
