package one.xis.server;

import one.xis.Action;
import one.xis.Parameter;
import one.xis.SharedValue;
import one.xis.UserContext;
import one.xis.UserContextImpl;
import one.xis.auth.token.SecurityAttributes;
import one.xis.deserialize.MainDeserializer;
import one.xis.deserialize.PostProcessingResults;
import one.xis.gson.JsonMap;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ControllerMethodParameterTest {

    @Test
    void unnamedParameterUsesPositionalActionArgumentsOnly() throws Exception {
        var method = TestActions.class.getDeclaredMethod("move", Object.class, String.class, String.class);
        var deserializer = mockDeserializer();
        var request = new ClientRequest();
        request.setActionParameters(JsonMap.of("$0", "\"a2\"", "$1", "\"a4\""));

        var fromParameter = new ControllerMethodParameter(method, method.getParameters()[1], deserializer, 1, 0);
        var toParameter = new ControllerMethodParameter(method, method.getParameters()[2], deserializer, 2, 1);

        assertThat(fromParameter.prepareParameter(request, new PostProcessingResults(), new HashMap<>())).isEqualTo("a2");
        assertThat(toParameter.prepareParameter(request, new PostProcessingResults(), new HashMap<>())).isEqualTo("a4");
    }

    @Test
    void explicitParameterIndexIsOneBased() throws Exception {
        var method = TestActions.class.getDeclaredMethod("moveWithExplicitIndexes", String.class, String.class);
        var deserializer = mockDeserializer();
        var request = new ClientRequest();
        request.setActionParameters(JsonMap.of("$0", "\"a2\"", "$1", "\"a4\""));

        var toParameter = new ControllerMethodParameter(method, method.getParameters()[0], deserializer, 0, -1);
        var fromParameter = new ControllerMethodParameter(method, method.getParameters()[1], deserializer, 1, -1);

        assertThat(toParameter.prepareParameter(request, new PostProcessingResults(), new HashMap<>())).isEqualTo("a4");
        assertThat(fromParameter.prepareParameter(request, new PostProcessingResults(), new HashMap<>())).isEqualTo("a2");
    }

    @Test
    void actionParameterIndexZeroFailsBecauseIndexesAreOneBased() throws Exception {
        var method = TestActions.class.getDeclaredMethod("invalidZeroIndex", String.class);
        var deserializer = mockDeserializer();
        var request = new ClientRequest();
        request.setActionParameters(JsonMap.of("$0", "\"a2\""));
        var parameter = new ControllerMethodParameter(method, method.getParameters()[0], deserializer, 0, -1);

        assertThatThrownBy(() -> parameter.prepareParameter(request, new PostProcessingResults(), new HashMap<>()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("index is 1-based");
    }

    @Test
    void userContextDoesNotCountAsPositionalActionArgument() throws Exception {
        var method = TestActions.class.getDeclaredMethod("moveWithUserContext", UserContext.class, String.class, String.class);
        var controllerMethod = new ControllerMethod(method, mockDeserializer(), mock(ControllerMethodResultMapper.class));
        var request = new ClientRequest();
        request.setActionParameters(JsonMap.of("$0", "\"a2\"", "$1", "\"a4\""));
        var controller = new TestActions();
        var securityAttributes = mock(SecurityAttributes.class);
        when(securityAttributes.getRoles()).thenReturn(Set.of());
        UserContextImpl.getInstance().setSecurityAttributes(securityAttributes);

        controllerMethod.invoke(request, controller, new HashMap<>());

        assertThat(controller.userContext).isSameAs(UserContext.getInstance());
        assertThat(controller.from).isEqualTo("a2");
        assertThat(controller.to).isEqualTo("a4");
    }

    private MainDeserializer mockDeserializer() {
        var deserializer = mock(MainDeserializer.class);
        when(deserializer.deserialize(eq("\"a2\""), any(), any(), any())).thenReturn("a2");
        when(deserializer.deserialize(eq("\"a4\""), any(), any(), any())).thenReturn("a4");
        return deserializer;
    }

    static class TestActions {

        UserContext userContext;
        String from;
        String to;

        @Action
        void move(@SharedValue("game") Object game, @Parameter String from, @Parameter String to) {
        }

        @Action
        void moveWithUserContext(UserContext userContext, @Parameter String from, @Parameter String to) {
            this.userContext = userContext;
            this.from = from;
            this.to = to;
        }

        @Action
        void moveWithExplicitIndexes(@Parameter(index = 2) String to, @Parameter(index = 1) String from) {
        }

        @Action
        void invalidZeroIndex(@Parameter(index = 0) String ignored) {
        }
    }
}
