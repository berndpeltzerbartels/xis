package one.xis.parameter;

import lombok.Value;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

@Value
class ParameterizedTargetElement implements Target {
    String name;
    ParameterizedType type;

    @Override
    public Class<?> getType() {
        return (Class<?>) type.getRawType();
    }

    @Override
    public Type getElementType() {
        return type.getActualTypeArguments()[0];
    }

}
