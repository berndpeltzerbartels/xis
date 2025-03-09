package one.xis.utils.lang;

import lombok.NonNull;
import lombok.experimental.UtilityClass;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.WildcardType;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@UtilityClass
public class FieldUtil {

    public void setFieldValue(Object obj, String fieldName, Object value) {
        Class<?> clazz = obj.getClass();
        while (clazz != null && !clazz.equals(Object.class)) {
            Field field = getDeclaredField(clazz, fieldName);
            if (field != null) {
                setFieldValue(obj, field, value);
            }
            clazz = clazz.getSuperclass();
        }
    }

    public Field getDeclaredField(Class<?> clazz, String fieldName) {
        return getDeclaredField(clazz, fieldName, true);
    }

    public List<Field> getFields(Class<?> clazz, Predicate<Field> filter) {
        var fields = new ArrayList<Field>();
        Class<?> c = clazz;
        while (c != null && !c.equals(Object.class)) {
            fields.addAll(getDeclaredFields(c).stream().filter(filter).toList());
            c = c.getSuperclass();
        }
        return fields;
    }

    public Collection<Field> getAllFields(Class<?> clazz) {
        Set<Field> fields = new HashSet<>();
        Class<?> c = clazz;
        while (c != null && !c.equals(Object.class)) {
            fields.addAll(getDeclaredFields(c));
            c = c.getSuperclass();
        }
        return Collections.unmodifiableSet(fields);
    }

    public Field getField(Class<?> clazz, String name) {
        Class<?> c = clazz;
        while (c != null && !c.equals(Object.class)) {
            var field = getDeclaredField(clazz, name);
            if (field != null) {
                return field;
            }
            c = c.getSuperclass();
        }
        return null;
    }


    public Collection<Field> getDeclaredFields(Class<?> clazz) {
        return Arrays.asList(clazz.getDeclaredFields());
    }

    public static Field getDeclaredField(Class<?> clazz, String name, boolean forceAccess) {
        try {
            Field field = clazz.getDeclaredField(name);
            if (forceAccess && !field.isAccessible()) {
                field.setAccessible(true);
            }
            return field;
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }

    public Collection<Field> getDeclaredAccessibleFields(Class<?> clazz) {
        return Arrays.stream(clazz.getDeclaredFields()).peek(f -> f.setAccessible(true))
                .collect(Collectors.toSet());
    }


    public void setFieldValue(@NonNull Object owner, @NonNull Field field, Object value) {
        field.setAccessible(true);
        try {
            field.set(owner, value);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public Class<?> getGenericTypeParameter(Field field) {
        var genericType = field.getGenericType();
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
        throw new IllegalArgumentException(field + " has no generic type");
    }


    public Class<?> getArrayComponentType(Field field) {
        return field.getType().getComponentType();
    }

    public boolean isIterableField(Field field) {
        return Iterable.class.isAssignableFrom(field.getType());
    }

    public boolean isArrayField(Field field) {
        return field.getType().isArray();
    }

    public static Object getFieldValue(Object owner, Field field) {
        field.setAccessible(true);
        try {
            return field.get(owner);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
