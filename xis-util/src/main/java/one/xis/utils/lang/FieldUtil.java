package one.xis.utils.lang;

import lombok.experimental.UtilityClass;

import java.lang.reflect.Field;
import java.util.*;

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
        }
        return Collections.unmodifiableSet(fields);
    }


    public Collection<Field> getDeclaredFields(Class<?> clazz) {
        return Arrays.asList(clazz.getDeclaredFields());
    }

    public void setFieldValue(Object owner, Field field, Object value) {
        field.setAccessible(true);
        try {
            field.set(owner, value);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public Class<?> getGenericTypeParameter(Field field) {
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
}
