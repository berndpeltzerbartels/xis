package on.xis.data;

import lombok.experimental.UtilityClass;

import java.lang.reflect.Field;

import static one.xis.utils.lang.FieldUtil.*;

@UtilityClass
class DataModelUtils {
    
    boolean isLeafField(Field field) {
        return isLeafType(field.getType());
    }


    boolean isLeafType(Class<?> type) {
        if (CharSequence.class.isAssignableFrom(type)) {
            return true;
        }
        if (type.isPrimitive()) {
            return true;
        }
        if (Number.class.isAssignableFrom(type)) {
            return true;
        }
        if (byte[].class.isAssignableFrom(type)) {
            return true;
        }
        return false;
    }

    boolean isIterableLeafField(Field field) {
        return isIterableField(field) && isLeafType(getGenericTypeParameter(field));
    }

    public static boolean isArrayLeafField(Field field) {
        return isArrayField(field) && isLeafType(field.getType().getComponentType());
    }

}
