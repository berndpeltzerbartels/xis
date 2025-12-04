package test.page.core;

import one.xis.context.IntegrationTestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SharedValueOrderTest {

    private MethodParameterOrderPageService service;
    private IntegrationTestContext testContext;

    @BeforeEach
    void init() {
        service = new MethodParameterOrderPageService();
        testContext = IntegrationTestContext.builder()
                .withSingleton(MethodParameterOrderPage.class)
                .withSingleton(service)
                .build();
    }

    @Test
    void testExecutionOrder() {
        var result = testContext.openPage("/requestScopeOrder.html");
        var calls = service.getRecorder().getCalls();
        // check the first three calls are a, b, c, this order is mandatory
        assertThat(calls).containsSubsequence("a", "b", "c");
        // check d and e are the last 2 elements in any order
        assertThat(calls.subList(calls.size() - 2, calls.size()))
                .containsExactlyInAnyOrder("d", "e");

    }
}
