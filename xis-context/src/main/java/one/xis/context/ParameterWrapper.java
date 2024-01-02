package one.xis.context;


import lombok.Getter;
import lombok.RequiredArgsConstructor;
import one.xis.utils.lang.MethodUtils;

import java.lang.reflect.Parameter;
import java.util.Collection;
import java.util.concurrent.ConcurrentLinkedQueue;

@Getter
@RequiredArgsConstructor
class ParameterWrapper extends ValueHolder {
    private final Parameter parameter;
    private final int index;
    private final ExecutableWrapper<?> parameterHolder;

    @Override
    Class<?> getType() {
        return parameter.getType();
    }

    @Override
    Class<?> getElementType() {
        if (Collection.class.isAssignableFrom(getType())) {
            return MethodUtils.getGenericTypeParameter(parameter);
        }
        if (getType().isArray()) {
            return parameter.getType().getComponentType();
        }
        return parameter.getType();
    }

    @Override
    void valueAssigned(Object o) {
        parameterHolder.parameterAssigned(o, this);
    }

    static Collection<ParameterWrapper> createParameterWrappers(Parameter[] parameters, ExecutableWrapper parameterHolder) {
        var rv = new ConcurrentLinkedQueue<ParameterWrapper>();
        for (int i = 0; i < parameters.length; i++) {
            rv.add(new ParameterWrapper(parameters[i], i, parameterHolder));
        }
        return rv;
    }

    @Override
    public String toString() {
        return "ParameterWrapper{" + parameter.getDeclaringExecutable() + ": " + parameter + "}";
    }


}
