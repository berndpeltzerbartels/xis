package one.xis.server;

import one.xis.ModelData;
import one.xis.SharedValue;
import one.xis.UserContext;
import one.xis.UserContextImpl;
import one.xis.deserialize.MainDeserializer;
import one.xis.http.RequestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.LinkedList;
import java.util.Queue;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class ControllerWrapperTest {

    private ControllerWrapper controllerWrapper;
    private ExampleController controller;

    @BeforeEach
    void setUp() {
        RequestContext.createInstance(mock(), mock());
        var deserializer = mock(MainDeserializer.class);
        var controllerResultMapper = mock(ControllerResultMapper.class);
        var wrapperFactory = new ControllerWrapperFactory(deserializer, new ControllerMethodResultMapper(mock(), new PathResolver()), controllerResultMapper, new PathResolver());
        controller = new ExampleController();
        controllerWrapper = wrapperFactory.createControllerWrapper("test", controller, ControllerWrapper.class);
        RequestContext.createInstance(mock(), mock());
        RequestContext.getInstance().setAttribute(UserContextImpl.CONTEXT_KEY, mock(UserContext.class));
    }

    @Test
    void invokeGetModelMethods() {
        controllerWrapper.invokeGetModelMethods(new ClientRequest(), new ControllerResult());

        // Verify the order of method invocations
        var invocationOrder = controller.getInvocationOrder();
        assertThat(invocationOrder).hasSize(3);
        assertThat(invocationOrder.poll()).isEqualTo("initializeQueue");
        assertThat(invocationOrder.poll()).isEqualTo("processQueue");
        assertThat(invocationOrder.poll()).isEqualTo("generateModel");
    }

    static class ExampleController {

        private final Queue<String> invocationOrder = new LinkedList<>();

        @SharedValue("queue")
        public Queue<String> initializeQueue() {
            invocationOrder.add("initializeQueue");
            return new LinkedList<>();
        }

        @SharedValue("processedQueue")
        public Queue<String> processQueue(@SharedValue("queue") Queue<String> queue) {
            invocationOrder.add("processQueue");
            queue.add("processed");
            return queue;
        }

        @ModelData("finalModel")
        public String generateModel(@SharedValue("processedQueue") Queue<String> processedQueue) {
            invocationOrder.add("generateModel");
            return "Model with data: " + processedQueue.toString();
        }

        public Queue<String> getInvocationOrder() {
            return invocationOrder;
        }
    }
}