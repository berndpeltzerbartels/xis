package test.page.router;

import one.xis.context.IntegrationTestContext;
import one.xis.UserContextImpl;
import one.xis.server.ClientConfigService;
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
    void routerRouteCanBeWelcomePage() {
        context = IntegrationTestContext.builder()
                .withSingleton(MethodWelcomeRouter.class)
                .withSingleton(RouterTargetPage.class)
                .build();

        var config = context.getAppContext().getSingleton(ClientConfigService.class).getConfig();
        assertThat(config.getWelcomePageId()).isEqualTo("/welcome-router/start.html");
        assertThat(config.getPageAttributes()).containsKey("/welcome-router/start.html");

        var response = processRouterRequest("/welcome-router/start.html", "/welcome-router/start.html");
        assertThat(response.getNextURL()).isEqualTo("/router-target/welcome.html?tab=start");
        assertThat(response.getData().get("targetId")).isEqualTo("welcome");
        assertThat(response.getData().get("targetTab")).isEqualTo("start");
    }

    @Test
    void routerWithOneRouteCanBeWelcomePage() {
        context = IntegrationTestContext.builder()
                .withSingleton(ClassWelcomeRouter.class)
                .withSingleton(RouterTargetPage.class)
                .build();

        var config = context.getAppContext().getSingleton(ClientConfigService.class).getConfig();
        assertThat(config.getWelcomePageId()).isEqualTo("/class-welcome-router/start.html");
        assertThat(config.getPageAttributes()).containsKey("/class-welcome-router/start.html");
    }

    @Test
    void routeMethodsAreRejectedOutsideRouterControllers() {
        assertThatThrownBy(() -> IntegrationTestContext.builder()
                .withSingleton(InvalidRoutePage.class)
                .build())
                .isInstanceOf(RuntimeException.class)
                .hasRootCauseMessage("@Route methods are only supported on @Router controllers: java.lang.String test.page.router.InvalidRoutePage.invalidRoute()");
    }

    @Test
    void routeMethodsRejectFrontletResponses() {
        assertThatThrownBy(() -> IntegrationTestContext.builder()
                .withSingleton(InvalidFrontletRouteRouter.class)
                .build())
                .isInstanceOf(RuntimeException.class)
                .hasRootCauseMessage("@Route method return type is not supported: one.xis.FrontletResponse test.page.router.InvalidFrontletRouteRouter.frontlet()");
    }

    @Test
    void routeMethodsRejectFrontletClassesAtRuntime() {
        context = IntegrationTestContext.builder()
                .withSingleton(InvalidFrontletClassRouteRouter.class)
                .withSingleton(RouterTargetFrontlet.class)
                .build();

        assertThatThrownBy(() -> processRouterRequest("/invalid-frontlet-class-route/frontlet.html", "/invalid-frontlet-class-route/frontlet.html"))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Failed to invoke route-method: ControllerMethod:class test.page.router.InvalidFrontletClassRouteRouter.frontletClass")
                .hasRootCauseMessage("@Route methods must navigate to pages, but returned: class test.page.router.RouterTargetFrontlet");
    }

    @Test
    void welcomePageMethodsAreRejectedOutsideRouterControllers() {
        assertThatThrownBy(() -> IntegrationTestContext.builder()
                .withSingleton(InvalidWelcomeMethodPage.class)
                .build())
                .isInstanceOf(RuntimeException.class)
                .hasRootCauseMessage("@WelcomePage methods are only supported on @Route methods inside @Router controllers: java.lang.String test.page.router.InvalidWelcomeMethodPage.invalidWelcomeMethod()");
    }

    @Test
    void welcomePageOnRouterClassRequiresExactlyOneRoute() {
        assertThatThrownBy(() -> IntegrationTestContext.builder()
                .withSingleton(InvalidWelcomeRouter.class)
                .build())
                .isInstanceOf(RuntimeException.class)
                .hasRootCauseMessage("@WelcomePage on @Router controllers requires exactly one @Route method: class test.page.router.InvalidWelcomeRouter");
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
