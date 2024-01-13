package one.xis.context;

import one.xis.utils.lang.MethodUtils;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashSet;
import java.util.stream.Collectors;

class BeanMethodWrapper extends ExecutableWrapper<Method> implements ComponentProducer {

    private final Collection<ComponentCreationListener> componentCreationListeners = new HashSet<>();
    private final Method method;
    private final ComponentWrapperPlaceholder placeholder;
    private final AppContextFactory factory;

    BeanMethodWrapper(Method method, ComponentWrapperPlaceholder placeholder, AppContextFactory factory) {
        super(method);
        this.method = method;
        this.placeholder = placeholder;
        this.factory = factory;
    }

    @Override
    public void addComponentCreationListener(ComponentCreationListener listener) {
        componentCreationListeners.add(listener);
    }

    @Override
    public Class<?> getResultClass() {
        return method.getReturnType();
    }

    @Override
    void parameterAssigned(Object o, ParameterWrapper parameter) {
        super.parameterAssigned(o, parameter);
        if (isPrepared()) {
            placeholder.beanMethodParameterSet(this);
        }
    }

    void execute(Object component) {
        var newComponent = MethodUtils.invoke(component, method, getArgs());
        componentCreationListeners.forEach(listener -> listener.componentCreated(newComponent, this));
        factory.componentCreated(newComponent, this);
    }

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder(getClass().getSimpleName());
        s.append("{");
        s.append(method.getName());
        s.append("(");
        s.append(getParameters().stream().map(ParameterWrapper::getType).map(Class::getSimpleName).collect(Collectors.joining(",")));
        s.append(")}");
        return s.toString();
    }


}
