package one.xis.parameter;

import lombok.Value;
import one.xis.FormData;
import one.xis.ModelData;

import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;

@Value
public class TargetParameter implements Target {

    Parameter parameter;

    @Override
    public String getName() {
        if (parameter.isAnnotationPresent(FormData.class)) {
            return parameter.getAnnotation(FormData.class).value();
        }
        if (parameter.isAnnotationPresent(ModelData.class)) {
            return parameter.getAnnotation(ModelData.class).value();
        }
        return parameter.getName();
    }

    @Override
    public Class<?> getType() {
        return parameter.getType();
    }

    @Override
    public Type getElementType() {
        var type = parameter.getParameterizedType();
        if (type instanceof ParameterizedType elementType) {
            return elementType.getActualTypeArguments()[0];
        }
        if (type instanceof WildcardType wildcardType) {
            return wildcardType.getUpperBounds()[0];
        }
        return null;
    }


}
