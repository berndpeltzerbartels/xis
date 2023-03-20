package one.xis.server;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import one.xis.Action;
import one.xis.Model;
import one.xis.ModelTimestamp;
import one.xis.context.XISComponent;
import one.xis.utils.lang.MethodUtils;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@XISComponent
@RequiredArgsConstructor
class ControllerWrapperFactory {

    ControllerWrapper createController(@NonNull String id, @NonNull Object controller) {
        try {
            var controllerWrapper = new ControllerWrapper();
            controllerWrapper.setId(id);
            controllerWrapper.setController(controller);
            controllerWrapper.setModelMethods(modelMethods(controller));
            controllerWrapper.setModelTimestampMethods(modelTimestampMethods(controller));
            controllerWrapper.setActionMethods(actionMethodMap(controller));
            controllerWrapper.setControllerClass(controller.getClass());
            return controllerWrapper;
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize " + controller.getClass(), e);
        }
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

    private Map<String, ModelTimestampMethod> modelTimestampMethods(Object controller) {
        return MethodUtils.methods(controller).stream()
                .filter(m -> m.isAnnotationPresent(ModelTimestamp.class))
                .map(this::createModelTimestampMethod)
                .collect(Collectors.toMap(ControllerMethod::getKey, Function.identity()));
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
                    .methodParameters(createParameters(method))
                    .build();
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize " + method, e);
        }
    }

    private ModelTimestampMethod createModelTimestampMethod(Method method) {
        method.setAccessible(true);
        try {
            return ModelTimestampMethod.builder()
                    .method(method)
                    .key(method.getAnnotation(ModelTimestamp.class).value())
                    .methodParameters(createParameters(method))
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
                    .methodParameters(createParameters(method))
                    .build();
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize " + method, e);
        }
    }

    private List<MethodParameter> createParameters(Method method) {
        return Arrays.stream(method.getParameters()).map(MethodParameter::createParameter).collect(Collectors.toList());
    }


}
