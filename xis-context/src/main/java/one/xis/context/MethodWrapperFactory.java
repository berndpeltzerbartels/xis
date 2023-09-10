package one.xis.context;

import lombok.RequiredArgsConstructor;

import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@RequiredArgsConstructor
class MethodWrapperFactory {

    private final ParameterFactory parameterFactory;
    private final Consumer<Object> componentConsumer;

    MethodWrapper methodWrapper(Object owner, Method method) {
        var componentParameters = parameterFactory.componentParameters(method).collect(Collectors.toCollection(ConcurrentLinkedDeque::new));
        return new MethodWrapper(owner, method, componentParameters, componentConsumer);
    }
}
