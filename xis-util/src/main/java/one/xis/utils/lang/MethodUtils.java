package one.xis.utils.lang;

import lombok.NonNull;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MethodUtils {

    public static final Predicate<Method> NON_PRIVATE = method -> !Modifier.isPrivate(method.getModifiers());

    public static Map<String, Method> findSettersByFieldName(@NonNull Class<?> clazz) {
        return methods(clazz, MethodUtils::isSetter)
                .stream()
                .collect(Collectors.toMap(MethodUtils::toFieldName, Function.identity(), (m1, m2) -> m1));
    }

    public static Map<String, Method> findGettersByFieldName(@NonNull Class<?> clazz) {
        return methods(clazz, MethodUtils::isGetter)
                .stream()
                .collect(Collectors.toMap(MethodUtils::toFieldName, Function.identity(), (m1, m2) -> m1));
    }

    public static Map<String, Method> findNonGettersOrSettersByFieldName(@NonNull Class<?> clazz) {
        return methods(clazz, method -> !isGetter(method) && !isSetter(method))
                .stream()
                .collect(Collectors.toMap(MethodUtils::methodSignature, Function.identity(), (m1, m2) -> m1));
    }


    private static boolean isSetter(Method method) {
        return method.getName().startsWith("set") && method.getParameterCount() == 1;
    }

    private static boolean isGetter(Method method) {
        return ((method.getReturnType().equals(Boolean.TYPE) && method.getName().startsWith("is")) || method.getName().startsWith("get")) && method.getParameterCount() == 0;
    }

    public static Optional<Method> findSetter(@NonNull Class<?> clazz, @NonNull String propertyName) {
        String methodName = "set" + StringUtils.firstToUpperCase(propertyName);
        Class<?> currentClass = clazz;
        while (currentClass != null && !currentClass.equals(Object.class)) {
            for (Method method : currentClass.getDeclaredMethods()) {
                if (method.getName().equals(methodName) && method.getParameterCount() == 1) {
                    return Optional.of(method);
                }
            }
            currentClass = currentClass.getSuperclass();
        }
        return Optional.empty();
    }


    public static Optional<Method> findGetter(@NonNull Class<?> clazz, @NonNull String propertyName) {
        String methodName = "get" + StringUtils.firstToUpperCase(propertyName);
        Class<?> currentClass = clazz;
        while (currentClass != null && !currentClass.equals(Object.class)) {
            for (Method method : currentClass.getDeclaredMethods()) {
                if (method.getName().equals(methodName) && method.getParameterCount() == 0) {
                    return Optional.of(method);
                }
            }
            currentClass = currentClass.getSuperclass();
        }
        return Optional.empty();
    }

    public static Method findMethod(@NonNull Class<?> clazz, @NonNull String methodName, Class<?>... parameterTypes) {
        Class<?> currentClass = clazz;
        while (currentClass != null && !currentClass.equals(Object.class)) {
            try {
                return currentClass.getDeclaredMethod(methodName, parameterTypes);
            } catch (NoSuchMethodException e) {
                // Methode in der aktuellen Klasse nicht gefunden, versuche die Superklasse
                currentClass = currentClass.getSuperclass();
            }
        }
        throw new RuntimeException("Method " + methodName + " not found in class " + clazz.getName() + " or its superclasses");
    }

    public static <A extends Annotation> Predicate<Method> annotatedWith(Class<A> annotationClass) {
        return method -> method.isAnnotationPresent(annotationClass);
    }

    public static Collection<Method> allMethods(@NonNull Object obj) {
        return allMethods(obj.getClass());
    }

    public static Collection<Method> allMethods(@NonNull Class<?> clazz) {
        Map<String, Method> methods = new HashMap<>();
        hierarchy(clazz).forEach(c -> declaredMethods(c).forEach(m -> methods.put(methodSignature(m), m)));
        return methods.values();
    }

    public static Collection<Method> methods(@NonNull Class<?> clazz, Predicate<Method> methodPredicate) {
        Map<String, Method> methods = new HashMap<>();
        hierarchy(clazz).forEach(c -> declaredMethods(c).filter(methodPredicate).forEach(m -> methods.put(methodSignature(m), m)));
        return methods.values();
    }

    public static String methodSignature(Method method) {
        return String.format("%s(%s)", method.getName(), parameterString(method));
    }

    public static Object invoke(Object o, Method method, Object... args) throws InvocationTargetException {
        try {
            method.setAccessible(true);
            return method.invoke(o, args);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public static Object doInvoke(Object o, Method method, Object... args) {
        try {
            return invoke(o, method, args);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e.getTargetException());
        }
    }

    public static Class<?> getGenericTypeParameter(Parameter parameter) {
        var genericType = parameter.getParameterizedType();
        if (genericType instanceof ParameterizedType parameterizedType) {
            var type = parameterizedType.getActualTypeArguments()[0];
            if (type instanceof WildcardType wildcardType) {
                return (Class<?>) wildcardType.getUpperBounds()[0];
            }
            var actualTypeparameter = parameterizedType.getActualTypeArguments()[0];
            if (actualTypeparameter instanceof ParameterizedType parameterizedType2) {
                return (Class<?>) parameterizedType2.getRawType(); // We do not want to dive deeper
            }
            return (Class<?>) actualTypeparameter;
        }
        throw new IllegalArgumentException(parameter + " has no generic type");
    }

    public static Class<?> getGenericTypeParameterOfReturnType(Method method) {
        var genericReturnType = method.getGenericReturnType();
        if (genericReturnType instanceof ParameterizedType parameterizedType) {
            var type = parameterizedType.getActualTypeArguments()[0];
            if (type instanceof WildcardType wildcardType) {
                return (Class<?>) wildcardType.getUpperBounds()[0];
            }
            if (type instanceof ParameterizedType parameterizedType2) {
                return (Class<?>) parameterizedType2.getRawType(); // We do not want to dive deeper
            }
            return (Class<?>) type;
        }
        throw new IllegalArgumentException(method + " has no generic return type");

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

    private static String toFieldName(Method method) {
        String name = method.getName();
        if (name.startsWith("is")) {
            return StringUtils.firstToLowerCase(name.substring(2));
        } else if (name.startsWith("set")) {
            return StringUtils.firstToLowerCase(name.substring(3));
        } else if (name.startsWith("get")) {
            return StringUtils.firstToLowerCase(name.substring(3));
        } else {
            throw new IllegalArgumentException("Method " + method.getName() + " is not a setter or getter");
        }
    }


}
