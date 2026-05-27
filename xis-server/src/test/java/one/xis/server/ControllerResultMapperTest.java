package one.xis.server;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ControllerResultMapperTest {

    @Test
    void sharedValuesStayRequestInternal() {
        var methodResult = new ControllerMethodResult();

        var controllerResult = new ControllerResult();
        controllerResult.getSharedValues().put("game", new Object());
        new ControllerResultMapper().mapMethodResultToControllerResult(methodResult, controllerResult);

        assertThat(controllerResult.getSharedValues()).containsKey("game");
    }
}
