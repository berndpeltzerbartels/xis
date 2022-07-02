package one.xis.context;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.lang.reflect.Parameter;
import java.util.Collection;

@RequiredArgsConstructor
public abstract class ConstructorParameter implements ComponentCreationListener {

    @Getter
    private final String name;

    static ConstructorParameter create(Parameter parameter) {
        if (parameter.getType().isArray()) {
            return new ArrayParameter(parameter);
        }
        if (Collection.class.isAssignableFrom(parameter.getType())) {
            return new CollectionParameter(parameter);
        }
        return new SimpleParameter(parameter.getType(), parameter.getName());
    }

    abstract Object getValue();


    abstract boolean isComplete();

    abstract Class<?> getElementType();
}
