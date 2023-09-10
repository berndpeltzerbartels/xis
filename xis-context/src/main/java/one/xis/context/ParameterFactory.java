package one.xis.context;

import lombok.RequiredArgsConstructor;

import java.lang.reflect.Executable;
import java.lang.reflect.Parameter;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Stream;

@RequiredArgsConstructor
class ParameterFactory {
    private final List<Class<?>> allComponentClasses;

    Stream<ComponentParameter> componentParameters(Executable executable) {
        var parameters = new HashSet<ComponentParameter>();
        for (var i = 0; i < executable.getParameterCount(); i++) {
            var parameter = executable.getParameters()[i];
            parameters.add(componentParameter(parameter, i));
        }
        return parameters.stream();
    }

    private ComponentParameter componentParameter(Parameter parameter, int index) {
        if (parameter.getType().isArray()) {
            return new ArrayParameter(parameter, index, allComponentClasses);
        }
        if (Collection.class.isAssignableFrom(parameter.getType())) {
            return new CollectionParameter(parameter, index, allComponentClasses);
        }
        return new SimpleParameter(parameter.getType(), index);
    }


}
