package one.xis.server;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import one.xis.Action;
import one.xis.Model;
import one.xis.context.XISComponent;
import one.xis.utils.lang.MethodUtils;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@XISComponent
@RequiredArgsConstructor
class ControllerWrapperFactory {

    private final ParameterDeserializer parameterDeserializer;

    ControllerWrapper createControllerWrapper(@NonNull String id, @NonNull Object controller) {
        try {
            var controllerWrapper = new ControllerWrapper();
            controllerWrapper.setId(id);
            controllerWrapper.setController(controller);
            controllerWrapper.setModelMethods(modelMethods(controller));
            controllerWrapper.setActionMethods(actionMethodMap(controller));
            controllerWrapper.setComponentAttributes(componentAttributes(controllerWrapper));
            return controllerWrapper;
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize " + controller.getClass(), e);
        }
    }

    private ComponentAttributes componentAttributes(ControllerWrapper wrapper) {
        var submitDataForModels = new HashSet<String>();
        wrapper.getModelMethods().values().forEach(method -> submitDataForModels.addAll(method.getRequiredModels()));
        var submitDataForActions = new HashMap<String, Collection<String>>();
        wrapper.getActionMethods().forEach((action, method) -> submitDataForActions.computeIfAbsent(action, a -> new HashSet<>()).addAll(method.getRequiredModels()));
        var attributes = new ComponentAttributes();
        attributes.setSubmitDataForModels(submitDataForModels);
        attributes.setSubmitDataForActions(submitDataForActions);
        return componentAttributes(wrapper);
    }

    private Map<String, ModelMethod> modelMethods(Object controller) {
        var map = new HashMap<String, ModelMethod>();
        MethodUtils.methods(controller).stream()
                .filter(m -> m.isAnnotationPresent(Model.class))
                .map(this::createModelMethod)
                .forEach(controllerMethod -> {
                    if (map.containsKey(controllerMethod.getKey())) {
                        throw new IllegalStateException(controller.getClass() + ": there is more than one @Model(...) annotation containing the key " + controllerMethod.key);
                    }
                    map.put(controllerMethod.getKey(), controllerMethod);
                });
        return map;

    }

    private Map<String, ActionMethod> actionMethodMap(Object controller) {
        return actionMethods(controller).collect(Collectors.toMap(ControllerMethod::getKey, Function.identity()));
    }

    private Stream<ActionMethod> actionMethods(Object controller) {
        return MethodUtils.methods(controller).stream()
                .filter(m -> m.isAnnotationPresent(Action.class))
                .map(this::createActionMethod);
    }

    private ModelMethod createModelMethod(Method method) {
        method.setAccessible(true);
        try {
            return ModelMethod.builder()
                    .method(method)
                    .key(method.getAnnotation(Model.class).value())
                    .parameterDeserializer(parameterDeserializer)
                    .build();
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize " + method, e);
        }
    }

    private ActionMethod createActionMethod(Method method) {
        method.setAccessible(true);
        try {
            return ActionMethod.builder()
                    .method(method)
                    .key(method.getAnnotation(Action.class).value())
                    .parameterDeserializer(parameterDeserializer)
                    .build();
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize " + method, e);
        }
    }

}
