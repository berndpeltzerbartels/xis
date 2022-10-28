package one.xis.controller;

import lombok.RequiredArgsConstructor;
import one.xis.context.XISComponent;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@XISComponent
@RequiredArgsConstructor
public class ControllerModelFactory {

    private final ControllerMethodFactory controllerMethodFactory;
    private final Map<Class<?>, ControllerModel> models = new HashMap<>();

    public ControllerModel controllerModel(Class<?> controllerClass) {
        return models.computeIfAbsent(controllerClass, this::createControllerModel);
    }

    private ControllerModel createControllerModel(Class<?> controllerClass) {
        return ControllerModel.builder()
                .controllerClass(controllerClass)
                .controllerMethods(controllerMethods(controllerClass))
                .build();
    }

    private Collection<ControllerMethod> controllerMethods(Class<?> controllerClass) {
        return controllerMethodFactory.controllerMethods(controllerClass).collect(Collectors.toSet());
    }
}
