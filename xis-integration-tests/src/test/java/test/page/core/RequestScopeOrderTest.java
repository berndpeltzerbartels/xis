package test.page.core;

import one.xis.context.IntegrationTestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class RequestScopeOrderTest {

    private RequestScopeOrderPageService service;
    private IntegrationTestContext testContext;

    @BeforeEach
    void init() {
        service = new RequestScopeOrderPageService();
        testContext = IntegrationTestContext.builder()
                .withSingleton(RequestScopeOrderPage.class)
                .withSingleton(service)
                .build();
    }

    @Test
    void testExecutionOrder() {
        var result = testContext.openPage("/requestScopeOrder.html");
        result.getDocument().getElementById("go").click();

        var calls = service.getRecorder().getCalls();
        /*
        TODO: Fix this test
        assertThat(calls).containsExactly(
                                     // ohne @RequestScope → zuerst
                "provideId",
                "normalModel", // request scoped → gebraucht für mehrere
                "provideToken",                     // dito
                "requestScopedModel: id=42",
                "form: token=abc",
                "scopedForm: id=42, token=abc",
                "go: id=42, token=abc"              // Action zuletzt
        );

         */
    }
}
