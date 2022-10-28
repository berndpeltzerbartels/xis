package one.xis.controller;

import lombok.Data;
import one.xis.InitModel;
import one.xis.common.RequestContext;
import one.xis.utils.lang.ClassUtils;

import java.lang.reflect.InvocationTargetException;

@Data
public class ControllerWrapper {
    private final Object contoller;
    private final ControllerModel controllerModel;

    public Object invokeInit(RequestContext context) {
        var model = createModelInstance();
        context.setState(model);
        controllerModel.getAnnotatedMethods(InitModel.class).forEach(m -> invoke(m, context));
        return model;
    }

    private Object invoke(ControllerMethod controllerMethod, RequestContext context) {
        var method = controllerMethod.getMethod(contoller);
        try {
            return method.invoke(contoller, controllerMethod.getArgs(context));
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    private Object createModelInstance() {
        return ClassUtils.newInstance(controllerModel.getModelType());
    }

}
