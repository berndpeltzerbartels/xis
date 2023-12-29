package one.xis.utils.lang;

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
}
