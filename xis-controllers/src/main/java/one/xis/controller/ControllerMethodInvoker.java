package one.xis.controller;

import lombok.Getter;
import lombok.NonNull;
import one.xis.Model;
import one.xis.State;
import one.xis.dto.ClientState;
import one.xis.dto.ComponentState;
import one.xis.dto.Request;
import one.xis.utils.lang.MethodUtils;

import javax.annotation.Nullable;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

import static one.xis.controller.ControllerUtils.getModelKey;

abstract class ControllerMethodInvoker {

    protected final Request request;
    protected final Object controller;
    protected ControllerParameterProvider parameterProvider;

    @Getter
    protected final ClientState clientState = new ClientState();

    @Getter
    protected final ComponentState componentState = new ComponentState();

    protected ControllerMethodInvoker(@NonNull Request request, @NonNull Object controller) {
        this.request = request;
        this.controller = controller;
        this.clientState.putAll(request.getClientState());
    }

    // TODO Convert primitives String etc
    protected Object paramValue(@NonNull Parameter parameter) {
        return parameterProvider.paramValue(parameter, request, clientState, componentState);
    }

    @Nullable
    protected Object invoke(@NonNull Method method) {
        var args = prepareArgs(method);
        @Nullable var rv = MethodUtils.invoke(controller, method, args);
        for (int i = 0; i < args.length; i++) {
            Parameter parameter = method.getParameters()[i];
            if (parameter.isAnnotationPresent(State.class)) {
                clientState.put(getModelKey(parameter), args[i]);
            }
            if (parameter.isAnnotationPresent(Model.class)) {
                componentState.put(getModelKey(parameter), args[i]);
            }
        }
        return rv;
    }

    private Object[] prepareArgs(@NonNull Method method) {
        var rv = new Object[method.getParameters().length];
        for (int i = 0; i < rv.length; i++) {
            rv[i] = paramValue(method.getParameters()[i]);
        }
        return rv;
    }

}
