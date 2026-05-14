package one.xis.utils.lang;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.RecordComponent;
import java.lang.reflect.WildcardType;

public class RecordUtil {
    public static Class<?> getGenericTypeParameter(RecordComponent recordComponent) {
        var genericType = recordComponent.getGenericType();
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
        throw new IllegalArgumentException(recordComponent + " has no generic type");
    }
}

