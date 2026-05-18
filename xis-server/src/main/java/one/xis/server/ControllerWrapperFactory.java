package one.xis.server;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import one.xis.*;
import one.xis.context.Component;
import one.xis.deserialize.MainDeserializer;
import one.xis.utils.lang.ClassUtils;
import one.xis.utils.lang.MethodUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
@RequiredArgsConstructor
class ControllerWrapperFactory {

    private final MainDeserializer deserializer;
    private final ControllerMethodResultMapper controllerMethodResultMapper;
    private final ControllerResultMapper controllerResultMapper;

    <W extends ControllerWrapper> W createControllerWrapper(@NonNull String id, @NonNull Object controller, Class<W> wrapperClass) {
        try {
            validateRouteAnnotations(controller);
            var controllerWrapper = ClassUtils.newInstance(wrapperClass);
            controllerWrapper.setId(id);
            controllerWrapper.setController(controller);
            controllerWrapper.setModelMethods(modelMethods(controller));
            controllerWrapper.setFormDataMethods(formDataMethods(controller));
            controllerWrapper.setActionMethods(actionMethodMap(controller));
            controllerWrapper.setSharedValueMethods(sharedValueMethods(controller));
            controllerWrapper.setTitleOnlyMethods(titleOnlyMethods(controller));
            controllerWrapper.setControllerResultMapper(controllerResultMapper);
            return controllerWrapper;
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize " + controller.getClass(), e);
        }
    }

    RouterControllerWrapper createRouterControllerWrapper(@NonNull Object controller) {
        var wrapper = createControllerWrapper(RouterUtil.getBaseUrl(controller), controller, RouterControllerWrapper.class);
        wrapper.setRouteMethods(routeMethodMap(controller));
        return wrapper;
    }

    private Collection<ControllerMethod> sharedValueMethods(Object controller) {
        return annotatedMethods(controller, SharedValue.class)
                .map(this::createControllerMethod)
                .collect(Collectors.toSet());
    }

    private Collection<ControllerMethod> modelMethods(Object controller) {
        return annotatedMethods(controller, ModelData.class)
                .filter(m -> !m.isAnnotationPresent(Action.class))
                .map(this::createControllerMethod)
                .collect(Collectors.toSet());
    }

    private Collection<ControllerMethod> formDataMethods(Object controller) {
        return annotatedMethods(controller, FormData.class)
                .filter(m -> !m.isAnnotationPresent(Action.class))
                .map(this::createControllerMethod)
                .collect(Collectors.toSet());
    }

    private <A extends Annotation> Stream<Method> annotatedMethods(Object controller, Class<A> annotation) {
        return MethodUtils.allMethods(controller).stream()
                .filter(m -> m.isAnnotationPresent(annotation));
    }

    private Map<String, ControllerMethod> actionMethodMap(Object controller) {
        return actionMethods(controller).collect(Collectors.toMap(this::getActionKey, Function.identity()));
    }

    private Map<String, ControllerMethod> routeMethodMap(Object controller) {
        return routeMethods(controller)
                .collect(Collectors.toMap(method -> RouterUtil.getRouteUrl(controller, method.getMethod()), Function.identity()));
    }

    private String getActionKey(ControllerMethod controllerMethod) {
        if (controllerMethod.getMethod().isAnnotationPresent(Action.class)) {
            var actionKey = controllerMethod.getMethod().getAnnotation(Action.class).value();
            if (actionKey.isEmpty()) {
                return controllerMethod.getMethod().getName();
            }
            return actionKey;
        }
        throw new IllegalStateException("Method is not annotated with Action: " + controllerMethod.getMethod());
    }

    private Stream<ControllerMethod> actionMethods(Object controller) {
        return MethodUtils.allMethods(controller).stream()
                .filter(m -> m.isAnnotationPresent(Action.class))
                .map(this::createControllerMethod);
    }

    private Stream<ControllerMethod> routeMethods(Object controller) {
        return MethodUtils.allMethods(controller).stream()
                .filter(m -> m.isAnnotationPresent(Route.class))
                .peek(this::validateRouteMethod)
                .map(this::createControllerMethod);
    }

    private void validateRouteAnnotations(Object controller) {
        if (controller.getClass().isAnnotationPresent(Router.class)) {
            return;
        }
        var routeMethod = MethodUtils.allMethods(controller).stream()
                .filter(method -> method.isAnnotationPresent(Route.class))
                .findFirst();
        routeMethod.ifPresent(method -> {
            throw new IllegalStateException("@Route methods are only supported on @Router controllers: " + method);
        });
    }

    private void validateRouteMethod(Method method) {
        if (method.isAnnotationPresent(Action.class)) {
            throw new IllegalStateException("@Route methods must not be annotated with @Action: " + method);
        }
        var returnType = method.getReturnType();
        if (returnType.equals(Void.TYPE) || returnType.equals(Void.class)) {
            throw new IllegalStateException("@Route methods must return a navigation value: " + method);
        }
        if (returnType.equals(String.class)
                || returnType.equals(Class.class)
                || PageResponse.class.isAssignableFrom(returnType)
                || PageUrlResponse.class.isAssignableFrom(returnType)
                || FrontletResponse.class.isAssignableFrom(returnType)
                || ModalResponse.class.isAssignableFrom(returnType)
                || RedirectControllerResponse.class.isAssignableFrom(returnType)) {
            return;
        }
        throw new IllegalStateException("@Route method return type is not supported: " + method);
    }

    private ControllerMethod createControllerMethod(Method method) {
        method.setAccessible(true);
        try {
            return new ControllerMethod(method, deserializer, controllerMethodResultMapper);
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize " + method, e);
        }
    }

    private Collection<ControllerMethod> titleOnlyMethods(@NonNull Object controller) {
        return annotatedMethods(controller, Title.class)
                .filter(m -> !m.isAnnotationPresent(Action.class))
                .filter(method -> !method.isAnnotationPresent(ModelData.class))
                .filter(method -> !method.isAnnotationPresent(FormData.class))
                .map(this::createControllerMethod)
                .collect(Collectors.toSet());
    }

}
