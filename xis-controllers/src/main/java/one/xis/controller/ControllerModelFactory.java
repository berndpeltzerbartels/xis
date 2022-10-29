package one.xis.controller;

import lombok.RequiredArgsConstructor;
import one.xis.context.XISComponent;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
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
        Collection<ControllerMethod> controllerMethods = controllerMethods(controllerClass);
        return ControllerModel.builder()
                .controllerClass(controllerClass)
                .controllerMethods(controllerMethods)
                .modelType(modelType(controllerMethods))
                .build();
    }

    private Class<?> modelType(Collection<ControllerMethod> controllerMethods) {
        var modelTypes = controllerMethods.stream()
                .map(ControllerMethod::getMethodParameters)
                .flatMap(List::stream)
                .filter(ModelParameter.class::isInstance)
                .map(ModelParameter.class::cast)
                .map(ModelParameter::getModelType)
                .collect(Collectors.toSet());
        switch (modelTypes.size()) {
            case 0:
                return Void.class;
            case 1:
                return modelTypes.iterator().next();
            default:
                throw new IllegalStateException("multiple model-types: " + modelTypes.stream().map(Class::getSimpleName).collect(Collectors.joining(", ")));
        }
    }

    private Collection<ControllerMethod> controllerMethods(Class<?> controllerClass) {
        return controllerMethodFactory.controllerMethods(controllerClass).collect(Collectors.toSet());
    }


}
