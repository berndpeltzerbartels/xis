package one.xis.utils.lang;

import lombok.experimental.UtilityClass;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@UtilityClass
public class MethodUtils {

    public Collection<Method> unprotectedMethods(Object obj) {
        return methods(obj).stream().filter(MethodUtils::unprotected).collect(Collectors.toList());
    }

    public Collection<Method> methods(Object obj) {
        Map<String, Method> methods = new HashMap<>();
        hierarchy(obj.getClass()).forEach(c -> methods(c).forEach(m -> methods.put(methodSignature(m), m)));
        return methods.values();
    }

    private List<Class<?>> hierarchy(Class<?> c) {
        List<Class<?>> classes = new ArrayList<>();
        while (c != null && c.equals(Object.class)) {
            classes.add(c);
            c = c.getSuperclass();
        }
        Collections.reverse(classes);
        return classes;
    }

    private Stream<Method> methods(Class<?> declaringClass) {
        return declaredMethods(declaringClass);
    }

    private boolean unprotected(Method method) {
        return !Modifier.isPrivate(method.getModifiers()) && !Modifier.isProtected(method.getModifiers());
    }

    private Stream<Method> declaredMethods(Class<?> type) {
        return Arrays.stream(type.getDeclaredMethods());
    }

    public String methodSignature(Method method) {
        return String.format("%s(%s)", method.getName(), parameterString(method));
    }

    private static String parameterString(Method method) {
        return Arrays.stream(method.getParameters()).map(Parameter::getType).map(Class::toString).collect(Collectors.joining(","));
    }
}
