package one.xis.context;


import lombok.Getter;
import lombok.Setter;

import java.lang.reflect.Constructor;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.stream.Collectors;

@Setter
@Getter
class ConstructorWrapper extends ExecutableWrapper<Constructor<?>> implements ComponentProducer, FieldHolder {

    private final Collection<ComponentCreationListener> componentCreationListeners = new HashSet<>();
    private final Constructor<?> constructor;
    private final AppContextFactory contextFactory;
    private final ComponentWrapperPlaceholder componentWrapperPlaceholder;
    private Collection<FieldWrapper> fieldWrappers;
    private Collection<InitMethodWrapper> initMethods;
    private Collection<BeanMethodWrapper> beanMethods;
    private boolean executed;

    ConstructorWrapper(Constructor<?> constructor,
                       ComponentWrapperPlaceholder componentWrapperPlaceholder,
                       AppContextFactory factory) {
        super(constructor);
        this.constructor = constructor;
        this.componentWrapperPlaceholder = componentWrapperPlaceholder;
        contextFactory = factory;
    }

    @Override
    public void mapProducers(Collection<ComponentProducer> producers) {
        fieldWrappers.forEach(fieldWrapper -> fieldWrapper.mapProducers(producers));
        initMethods.forEach(initMethods -> initMethods.mapProducers(producers));
        beanMethods.forEach(beanMethods -> beanMethods.mapProducers(producers));
        super.mapProducers(producers);
    }

    @Override
    public void mapInitialComponents(Collection<Object> components) {
        fieldWrappers.forEach(fieldWrapper -> fieldWrapper.mapInitialComponents(components));
        initMethods.forEach(initMethods -> initMethods.mapInitialComponents(components));
        beanMethods.forEach(beanMethods -> beanMethods.mapInitialComponents(components));
        super.mapInitialComponents(components);
    }

    @Override
    public void addComponentCreationListener(ComponentCreationListener listener) {
        componentCreationListeners.add(listener);
    }

    @Override
    public Class<?> getResultClass() {
        return constructor.getDeclaringClass();
    }

    @Override
    boolean parameterAssigned(Object o, ParameterWrapper parameter) {
        super.parameterAssigned(o, parameter);
        if (isPrepared()) {
            contextFactory.addExecutableConstructorWrapper(this);
            return true;
        }
        return false;
    }

    void execute() {
        contextFactory.removeExecutableConstructorWrapper(this);
        if (contextFactory.getReplacedClasses().contains(constructor.getDeclaringClass())) {
            var empty = new Empty();
            componentCreationListeners.forEach(listener -> listener.componentCreated(empty, this));
            contextFactory.componentCreated(empty, this);
        } else {
            constructor.setAccessible(true);
            try {
                executed = true;
                var component = constructor.newInstance(getArgs());
                componentCreationListeners.forEach(listener -> listener.componentCreated(component, this));
                contextFactory.componentCreated(component, this);
            } catch (Exception e) {
                throw new RuntimeException("", e);
            }
        }
    }

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder(getClass().getSimpleName());
        s.append("{");
        s.append(constructor.getDeclaringClass().getSimpleName());
        s.append("(");
        s.append(Arrays.stream(constructor.getParameters()).map(Parameter::getType).map(Class::getSimpleName).collect(Collectors.joining(",")));
        s.append(")}");
        return s.toString();
    }

}
