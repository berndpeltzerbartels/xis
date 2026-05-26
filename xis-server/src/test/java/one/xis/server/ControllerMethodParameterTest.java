package one.xis.server;

import one.xis.Action;
import one.xis.ActionParameter;
import one.xis.Frontend;
import one.xis.OwnedBy;
import one.xis.SharedValue;
import one.xis.ToastLevel;
import one.xis.UploadConfiguration;
import one.xis.UserContext;
import one.xis.UserContextImpl;
import one.xis.auth.AuthenticationException;
import one.xis.auth.AuthorizationException;
import one.xis.auth.token.SecurityAttributes;
import one.xis.deserialize.AccessDeniedError;
import one.xis.deserialize.DeserializationContext;
import one.xis.deserialize.MainDeserializer;
import one.xis.deserialize.PostProcessingResults;
import one.xis.gson.JsonMap;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ControllerMethodParameterTest {

    @Test
    void actionParameterRequiresNameOrIndex() throws Exception {
        var method = TestActions.class.getDeclaredMethod("move", Object.class, String.class, String.class);
        var deserializer = mockDeserializer();
        var request = new ClientRequest();
        request.setActionParameters(JsonMap.of("$0", "\"a2\"", "$1", "\"a4\""));

        var fromParameter = new ControllerMethodParameter(method, method.getParameters()[1], deserializer, 1, 0, mock(UploadConfiguration.class));

        assertThatThrownBy(() -> fromParameter.prepareParameter(request, new PostProcessingResults(), new HashMap<>()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("@ActionParameter must define value or index");
    }

    @Test
    void explicitParameterIndexIsOneBased() throws Exception {
        var method = TestActions.class.getDeclaredMethod("moveWithExplicitIndexes", String.class, String.class);
        var deserializer = mockDeserializer();
        var request = new ClientRequest();
        request.setActionParameters(JsonMap.of("$0", "\"a2\"", "$1", "\"a4\""));

        var toParameter = new ControllerMethodParameter(method, method.getParameters()[0], deserializer, 0, -1, mock(UploadConfiguration.class));
        var fromParameter = new ControllerMethodParameter(method, method.getParameters()[1], deserializer, 1, -1, mock(UploadConfiguration.class));

        assertThat(toParameter.prepareParameter(request, new PostProcessingResults(), new HashMap<>())).isEqualTo("a4");
        assertThat(fromParameter.prepareParameter(request, new PostProcessingResults(), new HashMap<>())).isEqualTo("a2");
    }

    @Test
    void actionParameterIndexZeroFailsBecauseIndexesAreOneBased() throws Exception {
        var method = TestActions.class.getDeclaredMethod("invalidZeroIndex", String.class);
        var deserializer = mockDeserializer();
        var request = new ClientRequest();
        request.setActionParameters(JsonMap.of("$0", "\"a2\""));
        var parameter = new ControllerMethodParameter(method, method.getParameters()[0], deserializer, 0, -1, mock(UploadConfiguration.class));

        assertThatThrownBy(() -> parameter.prepareParameter(request, new PostProcessingResults(), new HashMap<>()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("index is 1-based");
    }

    @Test
    void userContextDoesNotCountAsPositionalActionArgument() throws Exception {
        var method = TestActions.class.getDeclaredMethod("moveWithUserContext", UserContext.class, String.class, String.class);
        var controllerMethod = new ControllerMethod(method, mockDeserializer(), mock(ControllerMethodResultMapper.class), mock(UploadConfiguration.class));
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

    @Test
    void frontendParameterAddsModelFormDataAndToastMessagesToResult() throws Exception {
        var method = TestActions.class.getDeclaredMethod("frontend", Frontend.class);
        var parameter = new ControllerMethodParameter(method, method.getParameters()[0], mockDeserializer(), 0, -1, mock(UploadConfiguration.class));
        var frontend = (Frontend) parameter.prepareParameter(new ClientRequest(), new PostProcessingResults(), new HashMap<>());
        frontend.addModelData("pipeline", "Pipeline")
                .addFormData("step", "Step")
                .showToast("Saved", ToastLevel.SUCCESS);
        var result = new ControllerMethodResult();

        parameter.addParameterValueToResult(result, frontend, new ClientRequest());

        assertThat(result.getModelData()).containsEntry("pipeline", "Pipeline");
        assertThat(result.getFormData()).containsEntry("step", "Step");
        assertThat(result.getReturnedFormDataKeys()).containsExactly("step");
        assertThat(result.getToastMessages()).extracting("message").containsExactly("Saved");
        assertThat(result.getToastMessages()).extracting("level").containsExactly(ToastLevel.SUCCESS);
    }

    @Test
    void accessDeniedPostProcessingResultStopsInvocationWithAuthorizationException() throws Exception {
        var method = TestActions.class.getDeclaredMethod("secured");
        var controller = new TestActions();
        var controllerMethod = accessDeniedControllerMethod(method, false);
        setAuthenticatedContext();

        assertThatThrownBy(() -> controllerMethod.invoke(new ClientRequest(), controller, new HashMap<>()))
                .isInstanceOf(AuthorizationException.class)
                .hasMessageContaining("not owned");
        assertThat(controller.invoked).isFalse();
    }

    @Test
    void authenticationRequiredPostProcessingResultStopsInvocationWithAuthenticationException() throws Exception {
        var method = TestActions.class.getDeclaredMethod("secured");
        var controller = new TestActions();
        var controllerMethod = accessDeniedControllerMethod(method, true);
        setAuthenticatedContext();

        assertThatThrownBy(() -> controllerMethod.invoke(new ClientRequest(), controller, new HashMap<>()))
                .isInstanceOf(AuthenticationException.class)
                .isNotInstanceOf(AuthorizationException.class)
                .hasMessageContaining("not authenticated");
        assertThat(controller.invoked).isFalse();
    }

    private ControllerMethod accessDeniedControllerMethod(java.lang.reflect.Method method, boolean authenticationRequired) {
        return new ControllerMethod(method, mockDeserializer(), mock(ControllerMethodResultMapper.class), mock(UploadConfiguration.class)) {
            @Override
            protected Object[] prepareArgs(java.lang.reflect.Method method, ClientRequest request, PostProcessingResults postProcessingResults, Map<String, Object> requestScope) {
                var message = authenticationRequired ? "not authenticated" : "not owned";
                postProcessingResults.add(new AccessDeniedError(new DeserializationContext("/model", method, OwnedBy.class, UserContext.getInstance()), message, authenticationRequired));
                return new Object[0];
            }
        };
    }

    private void setAuthenticatedContext() {
        var securityAttributes = mock(SecurityAttributes.class);
        when(securityAttributes.getRoles()).thenReturn(Set.of());
        UserContextImpl.getInstance().setSecurityAttributes(securityAttributes);
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
        boolean invoked;

        @Action
        void move(@SharedValue("game") Object game, @ActionParameter String from, @ActionParameter String to) {
        }

        @Action
        void moveWithUserContext(UserContext userContext, @ActionParameter(index = 1) String from, @ActionParameter(index = 2) String to) {
            this.userContext = userContext;
            this.from = from;
            this.to = to;
        }

        @Action
        void moveWithExplicitIndexes(@ActionParameter(index = 2) String to, @ActionParameter(index = 1) String from) {
        }

        @Action
        void invalidZeroIndex(@ActionParameter(index = 0) String ignored) {
        }

        @Action
        void secured() {
            invoked = true;
        }

        @Action
        void frontend(Frontend frontend) {
        }
    }
}
