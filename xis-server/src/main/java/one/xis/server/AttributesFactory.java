package one.xis.server;

import one.xis.*;

import java.lang.reflect.Method;
import java.util.Arrays;

class AttributesFactory {

    void addParameterAttributes(Class<?> controllerClass, ComponentAttributes attributes) {
        for (var method : controllerClass.getDeclaredMethods()) {
            if (isAnnotatedFrameworkMethod(method)) {
                addParameterAttributes(method, attributes);
            }
        }
    }

    void addUpdateEventKeys(Class<?> controllerClass, ComponentAttributes attributes) {
        if (controllerClass.isAnnotationPresent(RefreshOnUpdateEvents.class)) {
            attributes.getUpdateEventKeys().addAll(Arrays.asList(controllerClass.getAnnotation(RefreshOnUpdateEvents.class).value()));
        }
    }

    void addParameterAttributes(Method method, ComponentAttributes attributes) {
        if (method.isAnnotationPresent(SessionStorage.class)) {
            attributes.getSessionStorageKeys().add(method.getAnnotation(SessionStorage.class).value());
        }
        if (method.isAnnotationPresent(LocalStorage.class)) {
            attributes.getLocalStorageKeys().add(method.getAnnotation(LocalStorage.class).value());
        }
        if (method.isAnnotationPresent(ClientState.class)) {
            attributes.getClientStateKeys().add(method.getAnnotation(ClientState.class).value());
        }
        for (var parameter : method.getParameters()) {
            if (parameter.isAnnotationPresent(SessionStorage.class)) {
                attributes.getSessionStorageKeys().add(parameter.getAnnotation(SessionStorage.class).value());
            }
            if (parameter.isAnnotationPresent(LocalStorage.class)) {
                attributes.getLocalStorageKeys().add(parameter.getAnnotation(LocalStorage.class).value());
            }
            if (parameter.isAnnotationPresent(ClientState.class)) {
                attributes.getClientStateKeys().add(parameter.getAnnotation(ClientState.class).value());
            }
        }
    }

    private boolean isAnnotatedFrameworkMethod(Method method) {
        return Arrays.stream(method.getAnnotations()).anyMatch(a -> a.annotationType().getPackageName().startsWith("one.xis"));
    }
}
