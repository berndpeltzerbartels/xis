package one.xis.controller;

import lombok.NonNull;
import one.xis.ClientAttribute;
import one.xis.ClientId;
import one.xis.Model;
import one.xis.UserId;
import one.xis.ajax.InvocationContext;
import one.xis.context.XISComponent;

import java.lang.reflect.Parameter;

import static one.xis.controller.ControllerUtils.getClientAttributeKey;

@XISComponent
class ControllerParameterProvider {

    Object paramValue(@NonNull Parameter parameter, @NonNull InvocationContext invocationContext) {
        if (parameter.isAnnotationPresent(ClientAttribute.class)) {
            String key = getClientAttributeKey(parameter);
            return null; //TODO
        }
        if (parameter.isAnnotationPresent(Model.class)) {
            return null; // TODO
        }
        if (parameter.isAnnotationPresent(UserId.class)) {
            return invocationContext.getClientAttributes().getUserId();
        }
        if (parameter.isAnnotationPresent(ClientId.class)) {
            return invocationContext.getClientAttributes().getClientId();
        }
        // TODO Widget/Page-Parameters
        return null;
    }
}
