package one.xis.utils.lang;

import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.WildcardType;

public class ParameterUtil {

    public static Class<?> getGenericTypeParameter(Parameter parameter) {
        var genericType = parameter.getParameterizedType();
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
        throw new IllegalArgumentException(parameter + " has no generic type");
    }

}
