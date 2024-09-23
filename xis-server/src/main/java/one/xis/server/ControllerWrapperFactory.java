package one.xis.server;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import one.xis.Action;
import one.xis.FormData;
import one.xis.ModelData;
import one.xis.context.XISComponent;
import one.xis.deserialize.MainDeserializer;
import one.xis.utils.lang.ClassUtils;
import one.xis.utils.lang.MethodUtils;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@XISComponent
@RequiredArgsConstructor
class ControllerWrapperFactory {

    private final MainDeserializer deserializer;
    private final ControllerMethodResultMapper controllerMethodResultMapper;
    private final ControllerResultMapper controllerResultMapper;

    <W extends ControllerWrapper> W createControllerWrapper(@NonNull String id, @NonNull Object controller, Class<W> wrapperClass) {
        try {
            var controllerWrapper = ClassUtils.newInstance(wrapperClass);
            controllerWrapper.setId(id);
            controllerWrapper.setController(controller);
            controllerWrapper.setModelMethods(modelMethods(controller));
            controllerWrapper.setActionMethods(actionMethodMap(controller));
            controllerWrapper.setControllerResultMapper(controllerResultMapper);
            return controllerWrapper;
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize " + controller.getClass(), e);
        }
    }

    private Map<String, ControllerMethod> modelMethods(Object controller) {
        var map = new HashMap<String, ControllerMethod>();
        MethodUtils.allMethods(controller).stream()
                .filter(m -> m.isAnnotationPresent(ModelData.class) || m.isAnnotationPresent(FormData.class))
                .map(this::createModelMethod)
                .forEach(controllerMethod -> {
                    if (map.containsKey(controllerMethod.getKey())) {
                        throw new IllegalStateException(controller.getClass() + ": there is more than one @ModelData or @FormData annotation assigning a value for the key " + controllerMethod.getKey());
                    }
                    map.put(controllerMethod.getKey(), controllerMethod);
                });
        return map;

    }

    private Map<String, ControllerMethod> actionMethodMap(Object controller) {
        return actionMethods(controller).collect(Collectors.toMap(ControllerMethod::getKey, Function.identity()));
    }

    private Stream<ControllerMethod> actionMethods(Object controller) {
        return MethodUtils.allMethods(controller).stream()
                .filter(m -> m.isAnnotationPresent(Action.class))
                .map(this::createActionMethod);
    }

    private ControllerMethod createModelMethod(Method method) {
        method.setAccessible(true);
        var key = method.isAnnotationPresent(FormData.class) ?
                method.getAnnotation(FormData.class).value() : method.getAnnotation(ModelData.class).value();
        try {
            return new ControllerMethod(method, key, deserializer, controllerMethodResultMapper);
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize " + method, e);
        }
    }

    private ControllerMethod createActionMethod(Method method) {
        method.setAccessible(true);
        try {
            var key = method.getAnnotation(Action.class).value();
            return new ControllerMethod(method, key, deserializer, controllerMethodResultMapper);
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize " + method, e);
        }
    }

}
