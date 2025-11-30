package one.xis.server;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import one.xis.*;
import one.xis.context.XISComponent;
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
            controllerWrapper.setRequestScopeMethods(requestScopeMethods(controller));
            controllerWrapper.setLocalStorageOnlyMethods(localStorageOnlyMethods(controller));
            controllerWrapper.setSessionStorageOnlyMethods(sessionStorageOnlyMethods(controller));
            controllerWrapper.setClientStorageOnlyMethods(clientStorageOnlyMethods(controller));
            controllerWrapper.setGlobalVariableOnlyMethods(globalVariableOnlyMethods(controller));
            controllerWrapper.setTagContentOnlyMethods(tagContentOnlyMethods(controller));
            controllerWrapper.setTitleOnlyMethods(titleOnlyMethods(controller));
            controllerWrapper.setAddressOnlyMethods(addressOnlyMethods(controller));
            controllerWrapper.setControllerResultMapper(controllerResultMapper);
            return controllerWrapper;
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize " + controller.getClass(), e);
        }
    }

    private Collection<ControllerMethod> localStorageOnlyMethods(@NonNull Object controller) {
        return annotatedMethods(controller, LocalStorage.class)
                .filter(m -> !m.isAnnotationPresent(Action.class))
                .filter(method -> !method.isAnnotationPresent(ModelData.class))
                .filter(method -> !method.isAnnotationPresent(FormData.class))
                .map(this::createControllerMethod)
                .collect(Collectors.toSet());
    }

    private Collection<ControllerMethod> sessionStorageOnlyMethods(@NonNull Object controller) {
        return annotatedMethods(controller, SessionStorage.class)
                .filter(m -> !m.isAnnotationPresent(Action.class))
                .filter(method -> !method.isAnnotationPresent(ModelData.class))
                .filter(method -> !method.isAnnotationPresent(FormData.class))
                .map(this::createControllerMethod)
                .collect(Collectors.toSet());
    }

    private Collection<ControllerMethod> clientStorageOnlyMethods(@NonNull Object controller) {
        return annotatedMethods(controller, ClientStorage.class)
                .filter(m -> !m.isAnnotationPresent(Action.class))
                .filter(method -> !method.isAnnotationPresent(ModelData.class))
                .filter(method -> !method.isAnnotationPresent(FormData.class))
                .map(this::createControllerMethod)
                .collect(Collectors.toSet());
    }

    private Collection<ControllerMethod> globalVariableOnlyMethods(@NonNull Object controller) {
        return annotatedMethods(controller, GlobalVariable.class)
                .filter(m -> !m.isAnnotationPresent(Action.class))
                .filter(method -> !method.isAnnotationPresent(ModelData.class))
                .filter(method -> !method.isAnnotationPresent(FormData.class))
                .map(this::createControllerMethod)
                .collect(Collectors.toSet());
    }

    private Collection<ControllerMethod> requestScopeMethods(Object controller) {
        return annotatedMethods(controller, MethodParameter.class)
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

    private ControllerMethod createControllerMethod(Method method) {
        method.setAccessible(true);
        try {
            return new ControllerMethod(method, deserializer, controllerMethodResultMapper);
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize " + method, e);
        }
    }

    private Collection<ControllerMethod> tagContentOnlyMethods(@NonNull Object controller) {
        return annotatedMethods(controller, TagContent.class)
                .filter(m -> !m.isAnnotationPresent(Action.class))
                .filter(method -> !method.isAnnotationPresent(ModelData.class))
                .filter(method -> !method.isAnnotationPresent(FormData.class))
                .map(this::createControllerMethod)
                .collect(Collectors.toSet());
    }

    private Collection<ControllerMethod> titleOnlyMethods(@NonNull Object controller) {
        return annotatedMethods(controller, Title.class)
                .filter(m -> !m.isAnnotationPresent(Action.class))
                .filter(method -> !method.isAnnotationPresent(ModelData.class))
                .filter(method -> !method.isAnnotationPresent(FormData.class))
                .map(this::createControllerMethod)
                .collect(Collectors.toSet());
    }

    private Collection<ControllerMethod> addressOnlyMethods(@NonNull Object controller) {
        return annotatedMethods(controller, Address.class)
                .filter(m -> !m.isAnnotationPresent(Action.class))
                .filter(method -> !method.isAnnotationPresent(ModelData.class))
                .filter(method -> !method.isAnnotationPresent(FormData.class))
                .map(this::createControllerMethod)
                .collect(Collectors.toSet());
    }
}
