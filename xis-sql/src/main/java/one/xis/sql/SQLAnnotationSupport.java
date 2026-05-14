package one.xis.sql;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.RecordComponent;

final class SQLAnnotationSupport {

    private SQLAnnotationSupport() {
    }

    static boolean ignored(Class<?> ownerType, Field field) {
        return hasAnnotation(ownerType, field, Ignore.class)
                || hasAnnotation(ownerType, field, NoColumn.class);
    }

    static boolean ignored(RecordComponent component) {
        return component.isAnnotationPresent(Ignore.class)
                || component.getAccessor().isAnnotationPresent(Ignore.class)
                || component.isAnnotationPresent(NoColumn.class)
                || component.getAccessor().isAnnotationPresent(NoColumn.class);
    }

    static boolean optionalColumn(Class<?> ownerType, Field field) {
        return hasAnnotation(ownerType, field, OptionalColumn.class);
    }

    static boolean optionalColumn(RecordComponent component) {
        return component.isAnnotationPresent(OptionalColumn.class)
                || component.getAccessor().isAnnotationPresent(OptionalColumn.class);
    }

    static boolean jsonColumn(Class<?> ownerType, Field field) {
        return hasAnnotation(ownerType, field, JsonColumn.class);
    }

    static boolean jsonColumn(RecordComponent component) {
        return component.isAnnotationPresent(JsonColumn.class) || component.getAccessor().isAnnotationPresent(JsonColumn.class);
    }

    private static boolean hasAnnotation(Class<?> ownerType, Field field, Class<? extends Annotation> annotationType) {
        return field.isAnnotationPresent(annotationType)
                || accessorHasAnnotation(ownerType, field, annotationType)
                || accessorHasAnnotation(field.getDeclaringClass(), field, annotationType);
    }

    private static boolean accessorHasAnnotation(Class<?> type, Field field, Class<? extends Annotation> annotationType) {
        String capitalized = capitalize(field.getName());
        return methodHasAnnotation(type, "get" + capitalized, annotationType)
                || methodHasAnnotation(type, "is" + capitalized, annotationType)
                || setterHasAnnotation(type, "set" + capitalized, field.getType(), annotationType);
    }

    private static boolean methodHasAnnotation(Class<?> type, String name, Class<? extends Annotation> annotationType) {
        for (Class<?> current = type; current != null; current = current.getSuperclass()) {
            for (Method method : current.getDeclaredMethods()) {
                if (method.getParameterCount() == 0 && method.getName().equals(name)
                        && method.isAnnotationPresent(annotationType)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean setterHasAnnotation(Class<?> type, String name, Class<?> parameterType,
                                               Class<? extends Annotation> annotationType) {
        for (Class<?> current = type; current != null; current = current.getSuperclass()) {
            for (Method method : current.getDeclaredMethods()) {
                if (method.getParameterCount() == 1 && method.getName().equals(name)
                        && method.getParameterTypes()[0].isAssignableFrom(parameterType)
                        && method.isAnnotationPresent(annotationType)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static String capitalize(String value) {
        if (value == null || value.isEmpty()) {
            return value;
        }
        return Character.toUpperCase(value.charAt(0)) + value.substring(1);
    }
}
