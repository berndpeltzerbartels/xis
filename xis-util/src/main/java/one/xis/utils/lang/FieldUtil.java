package one.xis.utils.lang;

import lombok.experimental.UtilityClass;

import java.lang.reflect.Field;

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

    public void setFieldValue(Object owner, Field field, Object value) {
        field.setAccessible(true);
        try {
            field.set(owner, value);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
