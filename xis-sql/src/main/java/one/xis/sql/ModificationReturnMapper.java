package one.xis.sql;

import java.lang.reflect.Method;
import java.math.BigInteger;
import java.util.Set;

class ModificationReturnMapper {

    private Method method;
    private Integer returnedParameterIndex;

    void init(Method method, String annotationName) {
        this.method = method;
        Class<?> returnType = method.getReturnType();
        if (returnType == Void.TYPE || returnType == Void.class || returnType == Boolean.TYPE || returnType == Boolean.class
                || integerReturnType(returnType)) {
            return;
        }
        returnedParameterIndex = returnedParameterIndex(method, returnType, annotationName);
    }

    Object map(int updateCount, Object[] args) {
        Class<?> returnType = method.getReturnType();
        if (returnType == Void.TYPE || returnType == Void.class) {
            return null;
        }
        if (returnType == Boolean.TYPE || returnType == Boolean.class) {
            return updateCount > 0;
        }
        if (returnType == Integer.TYPE || returnType == Integer.class) {
            return updateCount;
        }
        if (returnType == Long.TYPE || returnType == Long.class) {
            return (long) updateCount;
        }
        if (returnType == Short.TYPE || returnType == Short.class) {
            return (short) updateCount;
        }
        if (returnType == Byte.TYPE || returnType == Byte.class) {
            return (byte) updateCount;
        }
        if (returnType == Float.TYPE || returnType == Float.class) {
            return (float) updateCount;
        }
        if (returnType == Double.TYPE || returnType == Double.class) {
            return (double) updateCount;
        }
        if (returnType == BigInteger.class) {
            return BigInteger.valueOf(updateCount);
        }
        return args[returnedParameterIndex];
    }

    Set<Integer> returnedParameterIndexes() {
        return returnedParameterIndex == null ? Set.of() : Set.of(returnedParameterIndex);
    }

    private int returnedParameterIndex(Method method, Class<?> returnType, String annotationName) {
        int match = -1;
        Class<?>[] parameterTypes = method.getParameterTypes();
        for (int i = 0; i < parameterTypes.length; i++) {
            if (returnType.isAssignableFrom(parameterTypes[i])) {
                if (match >= 0) {
                    throw new IllegalArgumentException(annotationName + " return type " + returnType.getName()
                            + " matches multiple parameters on " + method);
                }
                match = i;
            }
        }
        if (match < 0) {
            throw new IllegalArgumentException(annotationName
                    + " return type must be void, boolean, integer number, or a parameter type: " + method);
        }
        return match;
    }

    private boolean integerReturnType(Class<?> type) {
        return type == Integer.TYPE || type == Integer.class
                || type == Long.TYPE || type == Long.class
                || type == Short.TYPE || type == Short.class
                || type == Byte.TYPE || type == Byte.class
                || type == BigInteger.class;
    }
}
