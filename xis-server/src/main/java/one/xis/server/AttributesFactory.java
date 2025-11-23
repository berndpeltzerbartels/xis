package one.xis.server;

import one.xis.GlobalVariable;
import one.xis.LocalDatabase;
import one.xis.LocalStorage;
import one.xis.SessionStorage;

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
            if (parameter.isAnnotationPresent(SessionStorage.class)) {
                attributes.getSessionStorageKeys().add(parameter.getAnnotation(SessionStorage.class).value());
            }
            if (parameter.isAnnotationPresent(LocalStorage.class)) {
                attributes.getLocalStorageKeys().add(parameter.getAnnotation(LocalStorage.class).value());
            }
            if (parameter.isAnnotationPresent(GlobalVariable.class)) {
                attributes.getGlobalVariableKeys().add(parameter.getAnnotation(GlobalVariable.class).value());
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
