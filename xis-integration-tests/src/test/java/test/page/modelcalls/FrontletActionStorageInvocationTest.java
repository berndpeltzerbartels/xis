package test.page.modelcalls;

import one.xis.context.IntegrationTestContext;
import one.xis.server.ClientRequest;
import one.xis.server.FrontendService;
import one.xis.server.RequestType;
import org.junit.jupiter.api.Test;

import java.time.ZoneId;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

class FrontletActionStorageInvocationTest {

    @Test
    void voidFrontletActionInvokesOwnModelAndStorageMethodsOnce() {
        var context = IntegrationTestContext.builder()
                .withPackage("test.page.modelcalls")
                .build();
        var response = context.getSingleton(FrontendService.class).processActionRequest(frontletActionRequest("voidAction"));
        var counter = context.getSingleton(ActionStorageInvocationCounter.class);

        assertThat(response.getData()).containsEntry("sourceModel", "model")
                .containsEntry("sourceModelAndState", "model-and-state");
        assertThat(response.getClientStateData()).containsEntry("sourceState", "state")
                .containsEntry("sourceModelAndState", "model-and-state");
        assertSourceActionInvocations(counter, "source.voidAction:source-token");
    }

    @Test
    void frontletActionReturningSameFrontletInvokesOwnModelAndStorageMethodsOnce() {
        var context = IntegrationTestContext.builder()
                .withPackage("test.page.modelcalls")
                .build();
        var response = context.getSingleton(FrontendService.class).processActionRequest(frontletActionRequest("sameFrontlet"));
        var counter = context.getSingleton(ActionStorageInvocationCounter.class);

        assertThat(response.getData()).containsEntry("sourceModel", "model")
                .containsEntry("sourceModelAndState", "model-and-state");
        assertThat(response.getClientStateData()).containsEntry("sourceState", "state")
                .containsEntry("sourceModelAndState", "model-and-state");
        assertSourceActionInvocations(counter, "source.sameFrontlet:source-token");
    }

    @Test
    void frontletActionReturningOtherFrontletInvokesOnlyTargetStorageMethods() {
        var context = IntegrationTestContext.builder()
                .withPackage("test.page.modelcalls")
                .build();
        var response = context.getSingleton(FrontendService.class).processActionRequest(frontletActionRequest("otherFrontlet"));
        var counter = context.getSingleton(ActionStorageInvocationCounter.class);

        assertThat(response.getData()).doesNotContainKey("targetModel")
                .containsEntry("targetModelAndState", "target-model-and-state");
        assertThat(response.getClientStateData()).containsEntry("targetState", "target-state")
                .containsEntry("targetModelAndState", "target-model-and-state");
        assertInvocationCount(counter, "source.shared", 1);
        assertInvocationCount(counter, "source.otherFrontlet:source-token", 1);
        assertInvocationCount(counter, "target.shared", 1);
        assertInvocationCount(counter, "target.state:target-token", 1);
        assertInvocationCount(counter, "target.modelAndState:target-token", 1);
        assertInvocationCount(counter, "target.model:target-token", 0);
    }

    private void assertSourceActionInvocations(ActionStorageInvocationCounter counter, String actionInvocation) {
        assertInvocationCount(counter, "source.shared", 1);
        assertInvocationCount(counter, actionInvocation, 1);
        assertInvocationCount(counter, "source.model:source-token", 1);
        assertInvocationCount(counter, "source.state:source-token", 1);
        assertInvocationCount(counter, "source.modelAndState:source-token", 1);
    }

    private void assertInvocationCount(ActionStorageInvocationCounter counter, String invocation, int count) {
        assertThat(Collections.frequency(counter.getInvocations(), invocation))
                .as(invocation)
                .isEqualTo(count);
    }

    private ClientRequest frontletActionRequest(String action) {
        var request = new ClientRequest();
        request.setClientId("frontlet-action-storage-invocation-test-client");
        request.setPageId("/action-storage-invocation.html");
        request.setPageUrl("/action-storage-invocation.html");
        request.setFrontletId("ActionStorageInvocationFrontlet");
        request.setType(RequestType.frontlet);
        request.setZoneId(ZoneId.systemDefault().getId());
        request.setAction(action);
        return request;
    }
}
