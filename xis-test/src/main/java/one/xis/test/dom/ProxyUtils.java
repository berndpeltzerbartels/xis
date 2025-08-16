// Datei: xis-test/src/main/java/one/xis/test/dom/ProxyUtils.java

package one.xis.test.dom;

import org.graalvm.polyglot.Value;

class ProxyUtils {
    @SuppressWarnings("unchecked")
    static <T> T convertValue(Class<T> targetType, Value value) {
        if (value.isNull()) {
            if (targetType.isPrimitive()) {
                throw new IllegalArgumentException("Cannot convert null/undefined to primitive type: " + targetType);
            }
            return null; // Null oder Undefined kann direkt in Referenztypen konvertiert werden
        }
        // Host-Objekt: direkter Java-Typ
        if (value.isHostObject()) {
            Object hostObj = value.asHostObject();
            if (targetType.isInstance(hostObj)) {
                return (T) hostObj;
            }
        }
        // Proxy-Objekt: z.B. JDK-Proxy, GraalVMProxy
        if (value.hasMembers() && value.isProxyObject()) {
            Object proxyObj = value.asProxyObject();
            if (targetType.isInstance(proxyObj)) {
                return (T) proxyObj;
            }
        }
        // Standard-Konvertierungen
        if (targetType == boolean.class || targetType == Boolean.class) {
            if (value.isBoolean()) {
                return (T) Boolean.valueOf(value.asBoolean());
            } else if (value.isString()) {
                String str = value.asString();
                return (T) Boolean.valueOf("true".equalsIgnoreCase(str) || "1".equals(str));
            } else if (value.isNumber()) {
                return (T) Boolean.valueOf(value.asInt() > 0);
            }
            throw new IllegalArgumentException("Cannot convert value to boolean: " + value);
        } else if (targetType == int.class || targetType == Integer.class) {
            if (value.isNumber()) {
                return (T) Integer.valueOf(value.asInt());
            } else if (value.isString()) {
                try {
                    return (T) Integer.valueOf(value.asString());
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("Cannot convert value to int: " + value, e);
                }
            }
            throw new IllegalArgumentException("Cannot convert value to int: " + value);
        } else if (targetType == String.class) {
            if (value.isString()) {
                return (T) value.asString();
            } else if (value.isNumber()) {
                if (value.fitsInInt()) {
                    return (T) String.valueOf(value.asInt());
                } else if (value.fitsInShort()) {
                    return (T) String.valueOf(value.asShort());
                } else if (value.fitsInLong()) {
                    return (T) String.valueOf(value.asLong());
                } else if (value.fitsInFloat()) {
                    return (T) String.valueOf(value.asFloat());
                } else if (value.fitsInBigInteger()) {
                    return (T) value.asBigInteger().toString();
                } else {
                    return (T) String.valueOf(value.asDouble());
                }
            } else if (value.isBoolean()) {
                return (T) String.valueOf(value.asBoolean());
            }
            throw new IllegalArgumentException("Cannot convert value to String: " + value);
        } else if (targetType.isEnum()) {
            return (T) Enum.valueOf((Class<Enum>) targetType, value.asString());
        }
        // Fallback: GraalVM-Konvertierung
        return value.as(targetType);
    }
}