package one.xis.utils.lang;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Set;

public class TypeUtils {


    static Set<Class<?>> NUMBER_PRIMITIVES = Set.of(
            Short.TYPE,
            Integer.TYPE,
            Long.TYPE,
            Float.TYPE,
            Double.TYPE
    );


    public static boolean isNumber(Class<?> clazz) {
        if (Number.class.isAssignableFrom(clazz)) {
            return true;
        }
        return NUMBER_PRIMITIVES.contains(clazz);
    }


    public static boolean isSimple(Class<?> clazz) {
        return clazz.isPrimitive() || clazz == String.class || clazz == BigDecimal.class || clazz == BigInteger.class ||
                clazz == Boolean.class || clazz == Character.class || isNumber(clazz);
    }

    public static Object convertSimple(Object o, Class<?> targetType) {
        if (o == null) {
            return null;
        }
        if (targetType.isInstance(o)) {
            return o;
        }
        if (targetType == String.class) {
            return o.toString();
        }
        if (targetType == Integer.class || targetType == Integer.TYPE) {
            return Integer.parseInt(o.toString());
        }
        if (targetType == Long.class || targetType == Long.TYPE) {
            return Long.parseLong(o.toString());
        }
        if (targetType == Double.class || targetType == Double.TYPE) {
            return Double.parseDouble(o.toString());
        }
        if (targetType == Float.class || targetType == Float.TYPE) {
            return Float.parseFloat(o.toString());
        }
        if (targetType == Short.class || targetType == Short.TYPE) {
            return Short.parseShort(o.toString());
        }
        if (targetType == Byte.class || targetType == Byte.TYPE) {
            return Byte.parseByte(o.toString());
        }
        if (targetType == BigInteger.class) {
            return new BigInteger(o.toString());
        }
        if (targetType == BigDecimal.class) {
            return new BigDecimal(o.toString());
        }
        if (targetType == Boolean.class || targetType == Boolean.TYPE) {
            return Boolean.parseBoolean(o.toString());
        }
        throw new IllegalArgumentException("Cannot convert " + o.getClass().getName() + " to " + targetType.getName());
    }
}
