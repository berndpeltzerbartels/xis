package one.xis.context;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.function.Consumer;
import java.util.stream.Collectors;


class ComponentWrapperFactory {

    private final ComponentClassReflector componentClassReflector;
    private final MethodWrapperFactory methodWrapperFactory;
    private final FieldWrapperFactory fieldWrapperFactory;

    ComponentWrapperFactory(ParameterFactory parameterFactory,
                            ComponentClassReflector componentClassReflector,
                            FieldWrapperFactory fieldWrapperFactory,
                            Consumer<Object> componentConsumer) {
        this.componentClassReflector = componentClassReflector;
        this.fieldWrapperFactory = fieldWrapperFactory;
        this.methodWrapperFactory = new MethodWrapperFactory(parameterFactory, componentConsumer);
    }

    ComponentWrapper createComponentWrapper(Object component) {
        var methodWrappers = methodWrappers(component);
        var fieldWrappers = componentClassReflector.dependencyFields(component.getClass())
                .map(field -> fieldWrapperFactory.createFieldWrapper(field, component))
                .collect(Collectors.toCollection(ConcurrentLinkedDeque::new));
        return new ComponentWrapper(methodWrappers, fieldWrappers);
    }

    private Queue<MethodWrapper> methodWrappers(Object o) {
        return componentClassReflector.annotatedMethods(o.getClass())
                .map(method -> methodWrapperFactory.methodWrapper(o, method))
                .collect(Collectors.toCollection(ConcurrentLinkedDeque::new));

    }


}
