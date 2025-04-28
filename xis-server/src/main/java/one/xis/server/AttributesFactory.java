package one.xis.server;

import one.xis.ClientState;
import one.xis.LocalDatabase;
import one.xis.LocalStorage;

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

    private void addParameterAttributes(Method method, ComponentAttributes attributes) {
        for (var parameter : method.getParameters()) {
            if (parameter.isAnnotationPresent(ClientState.class)) {
                attributes.getClientStateKeys().add(parameter.getAnnotation(ClientState.class).value());
            }
            if (parameter.isAnnotationPresent(LocalStorage.class)) {
                attributes.getLocalStorageKeys().add(parameter.getAnnotation(LocalStorage.class).value());
            }
            if (parameter.isAnnotationPresent(LocalDatabase.class)) {
                attributes.getLocalDatabaseKeys().add(parameter.getAnnotation(LocalDatabase.class).value());
            }
        }
    }

    private boolean isAnnotatedFrameworkMethod(Method method) {
        return Arrays.stream(method.getAnnotations()).anyMatch(a -> a.annotationType().getPackageName().startsWith("one.xis"));
    }
}
