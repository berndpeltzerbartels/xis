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

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Collection;
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
            controllerWrapper.setFormDataMethods(formDataMethods(controller));
            controllerWrapper.setActionMethods(actionMethodMap(controller));
            controllerWrapper.setControllerResultMapper(controllerResultMapper);
            return controllerWrapper;
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize " + controller.getClass(), e);
        }
    }

    private Collection<ControllerMethod> modelMethods(Object controller) {
        var map = new MethodMap();
        annotatedMethods(controller, ModelData.class)
                .map(this::createModelMethod)
                .forEach(controllerMethod -> map.put(controllerMethod.getKey(), controllerMethod));
        return map.values();
    }

    private Collection<ControllerMethod> formDataMethods(Object controller) {
        var map = new MethodMap();
        annotatedMethods(controller, FormData.class)
                .map(this::createFormDataMethod)
                .forEach(controllerMethod -> map.put(controllerMethod.getKey(), controllerMethod));
        return map.values();
    }

    private static class MethodMap extends HashMap<String, ControllerMethod> {
        void put(ControllerMethod controllerMethod) {
            if (containsKey(controllerMethod.getKey())) {
                throw new IllegalStateException("Duplicate method key: " + controllerMethod.getKey());
            }
            super.put(controllerMethod.getKey(), controllerMethod);
        }
    }

    private <A extends Annotation> Stream<Method> annotatedMethods(Object controller, Class<A> annotation) {
        return MethodUtils.allMethods(controller).stream()
                .filter(m -> m.isAnnotationPresent(annotation));
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
        var key = method.getAnnotation(ModelData.class).value();
        try {
            return new ControllerMethod(method, key, deserializer, controllerMethodResultMapper);
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize " + method, e);
        }
    }

    private ControllerMethod createFormDataMethod(Method method) {
        method.setAccessible(true);
        var key = method.getAnnotation(FormData.class).value();
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
