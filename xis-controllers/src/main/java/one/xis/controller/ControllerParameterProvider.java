package one.xis.controller;

import lombok.NonNull;
import one.xis.*;
import one.xis.context.XISComponent;
import one.xis.dto.ClientState;
import one.xis.dto.ComponentState;
import one.xis.dto.Request;
import one.xis.utils.lang.ClassUtils;

import java.lang.reflect.Parameter;

import static one.xis.controller.ControllerUtils.getModelKey;
import static one.xis.controller.ControllerUtils.getStateKey;

@XISComponent
class ControllerParameterProvider {

    Object paramValue(@NonNull Parameter parameter, @NonNull Request request, @NonNull ClientState clientState, @NonNull ComponentState componentState) {
        if (parameter.isAnnotationPresent(State.class)) {
            String key = getStateKey(parameter);
            return clientState.containsKey(key) ? clientState.get(key) : ClassUtils.newInstance(parameter.getType());
        }
        if (parameter.isAnnotationPresent(Model.class)) {
            return componentState.get(getModelKey(parameter));
        }
        if (parameter.isAnnotationPresent(UserId.class)) {
            return null; // TODO
        }
        if (parameter.isAnnotationPresent(Token.class)) {
            return request.getToken();
        }
        if (parameter.isAnnotationPresent(ClientId.class)) {
            return request.getClientId();
        }
        // TODO Widget/Page-Parameters
        return null;
    }
}
