package test.page.core;

import one.xis.context.IntegrationTestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

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
        var expected = new String[]{"a", "b", "c", "d", "a", "b", "go", "c", "d"};


        assertThat(calls).containsExactly(expected);
    }
}
