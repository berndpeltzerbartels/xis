package one.xis.utils.lang;

import lombok.NonNull;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MethodUtils {

    public static final Predicate<Method> NON_PRIVATE = method -> !Modifier.isPrivate(method.getModifiers());

    public static <A extends Annotation> Predicate<Method> annotatedWith(Class<A> annotationClass) {
        return method -> method.isAnnotationPresent(annotationClass);
    }

    public static Collection<Method> methods(@NonNull Object obj) {
        return methods(obj.getClass());
    }

    public static Collection<Method> methods(@NonNull Class<?> clazz) {
        Map<String, Method> methods = new HashMap<>();
        hierarchy(clazz).forEach(c -> declaredMethods(c).forEach(m -> methods.put(methodSignature(m), m)));
        return methods.values();
    }

    public static String methodSignature(Method method) {
        return String.format("%s(%s)", method.getName(), parameterString(method));
    }

    public static Object invoke(Object controller, Method method, Object[] args) {
        try {
            return method.invoke(controller, args);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    public static Class<?> getGenericTypeParameter(Parameter parameter) {
        var parameterizedType = (ParameterizedType) parameter.getParameterizedType();
        return (Class<?>) parameterizedType.getActualTypeArguments()[0];
    }


    /**
     * Creates a list containing the given class and it's superclasses
     * so the most parent class is first and the given subclass is the last.
     * <p>
     * This is the order we need to replace super-method by overriden method.
     *
     * @param c
     * @return
     */
    public static List<Class<?>> hierarchy(Class<?> c) {
        List<Class<?>> classes = new ArrayList<>();
        while (c != null && !c.equals(Object.class)) {
            classes.add(c);
            c = c.getSuperclass();
        }
        Collections.reverse(classes);
        return classes;
    }

    private static Stream<Method> declaredMethods(Class<?> type) {
        return Arrays.stream(type.getDeclaredMethods());
    }

    private static String parameterString(Method method) {
        return Arrays.stream(method.getParameters()).map(Parameter::getType).map(Class::toString).collect(Collectors.joining(","));
    }


}
