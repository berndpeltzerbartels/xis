package one.xis.server;

import one.xis.LocalDatabase;
import one.xis.LocalStorage;
import one.xis.PageScope;

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
            if (parameter.isAnnotationPresent(PageScope.class)) {
                attributes.getPageScope().add(parameter.getAnnotation(PageScope.class).value());
            }
            if (parameter.isAnnotationPresent(LocalStorage.class)) {
                attributes.getLocalStorage().add(parameter.getAnnotation(one.xis.LocalStorage.class).value());
            }
            if (parameter.isAnnotationPresent(LocalDatabase.class)) {
                attributes.getLocalDatabase().add(parameter.getAnnotation(one.xis.LocalDatabase.class).value());
            }
        }
    }

    private boolean isAnnotatedFrameworkMethod(Method method) {
        return Arrays.stream(method.getAnnotations()).anyMatch(a -> a.annotationType().getPackageName().startsWith("one.xis"));
    }
}
