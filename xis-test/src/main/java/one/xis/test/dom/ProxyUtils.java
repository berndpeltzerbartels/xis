package one.xis.test.dom;

import org.graalvm.polyglot.Value;

class ProxyUtils {
    static Object convertValue(Class<?> targetType, Value value) {
        if (targetType == boolean.class || targetType == Boolean.class) {
            return value.asBoolean();
        } else if (targetType == int.class || targetType == Integer.class) {
            return value.asInt();
        } else if (targetType == double.class || targetType == Double.class) {
            return value.asDouble();
        } else if (targetType == String.class) {
            return value.asString();
        } else if (targetType.isEnum()) {
            return Enum.valueOf((Class<Enum>) targetType, value.asString());
        }
        throw new IllegalArgumentException("Unsupported type for setter: " + targetType.getName());
    }
}
