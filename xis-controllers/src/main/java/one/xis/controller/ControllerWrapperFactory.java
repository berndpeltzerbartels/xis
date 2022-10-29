package one.xis.controller;


import one.xis.Model;
import one.xis.context.XISComponent;
import one.xis.utils.lang.CollectorUtils;

import java.lang.reflect.Method;
import java.util.Optional;

@XISComponent
public class ControllerWrapperFactory {

    public ControllerWrapper createControllerWrapper(Object controller, ControllerModel controllerModel) {
        return null;
    }

    private Optional<Method> getModelFactoryMethod(Object controller, ControllerModel controllerModel) {
        return controllerModel.getAnnotatedMethods(Model.class)
                .map(controllerMethod -> controllerMethod.getMethod(controller))
                .collect(CollectorUtils.toOnlyOptional(list -> new IllegalStateException(controller + "contains more than one methods annotated with @Model")));
    }
}
