package one.xis.server;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ControllerResultMapperTest {

    @Test
    void sharedValuesStayRequestInternal() {
        var methodResult = new ControllerMethodResult();
        methodResult.getRequestScope().put("game", new Object());

        var controllerResult = new ControllerResult();
        new ControllerResultMapper().mapMethodResultToControllerResult(methodResult, controllerResult);

        assertThat(controllerResult.getRequestScope()).isEmpty();
    }
}
