package one.xis.context2;

import lombok.RequiredArgsConstructor;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * This class encapsulates annotation reflection for a framework. It is used to determine
 * if a method is annotated as a bean producer or initializer,
 * if a class is annotated, and if a field is a dependency.
 */
@RequiredArgsConstructor
class Annotations {

    /**
     * Determines if a method is annotated as a bean producer or initializer.
     *
     * @param method
     * @return
     */
    boolean isAnnotatedMethod(Method method) {
        return true;
    }

    /**
     * Determines if a class is a component.
     *
     * @param c
     * @return
     */
    boolean isAnnotatedComponent(Class<?> c) {
        return true;
    }

    /**
     * Determines if a field is a dependency.
     *
     * @param field
     * @return
     */
    boolean isDependencyField(Field field) {
        return true;
    }

}
