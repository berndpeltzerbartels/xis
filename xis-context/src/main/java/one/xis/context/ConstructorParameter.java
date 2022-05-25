package one.xis.context;

import java.lang.reflect.Parameter;
import java.util.Collection;

public abstract class ConstructorParameter {
    abstract boolean onComponentCreated(Object o);

    static ConstructorParameter create(Parameter parameter) {
        if (parameter.getType().isArray()) {
            return new ArrayParameter(parameter);
        }
        if (Collection.class.isAssignableFrom(parameter.getType())) {
            return new CollectionParameter(parameter);
        }
        return new SimpleParameter(parameter.getType());
    }

    abstract Object getValue();
}
