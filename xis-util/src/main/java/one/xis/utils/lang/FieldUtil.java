package one.xis.utils.lang;

import lombok.experimental.UtilityClass;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.*;
import java.util.stream.Collectors;

@UtilityClass
public class FieldUtil {

    public void setFieldValue(Object obj, String fieldName, Object value) {
        Class<?> clazz = obj.getClass();
        while (clazz != null && clazz.equals(Object.class)) {
            Field field = getDeclaredField(clazz, fieldName);
            if (field != null) {
                setFieldValue(obj, field, value);
            }
            clazz = clazz.getSuperclass();
        }
    }

    public Field getDeclaredField(Class<?> clazz, String fieldName) {
        try {
            return clazz.getDeclaredField(fieldName);
        } catch (NoSuchFieldException e) {
            return null;
        }
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

    public Collection<Field> getDeclaredAccessibleFields(Class<?> clazz) {
        return Arrays.asList(clazz.getDeclaredFields())
                .stream().peek(f -> f.setAccessible(true))
                .collect(Collectors.toSet());
    }


    public void setFieldValue(Object owner, Field field, Object value) {
        field.setAccessible(true);
        try {
            field.set(owner, value);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public Type getGenericTypeParameter(Field field) {
        return null;
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
