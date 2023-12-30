package one.xis.parameter;

import lombok.Value;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;

@Value
class TargetField implements Target {
    Field field;

    @Override
    public String getName() {
        return field.getName();
    }

    @Override
    public Class<?> getType() {
        return field.getType();
    }

    @Override
    public Type getElementType() {
        var type = field.getGenericType();
        if (type instanceof ParameterizedType elementType) {
            return elementType.getActualTypeArguments()[0];
        }
        if (type instanceof WildcardType wildcardType) {
            return wildcardType.getUpperBounds()[0];
        }
        return null;
    }

}
