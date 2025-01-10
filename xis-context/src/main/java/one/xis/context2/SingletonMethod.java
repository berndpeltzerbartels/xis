package one.xis.context2;

import one.xis.utils.lang.MethodUtils;

import java.lang.reflect.Method;

class SingletonMethod extends SingletonProducerImpl {
    private final Method method;
    private final SingletonWrapper parent;

    SingletonMethod(Method method, SingletonWrapper parent, ParameterFactory parameterFactory) {
        super(method.getParameters(), parameterFactory);
        this.method = method;
        this.parent = parent;
    }


    @Override
    public Class<?> getSingletonClass() {
        return null;
    }

    @Override
    public void assignValue(Object o) {

    }

    @Override
    public boolean isConsumerFor(Class<?> c) {
        return false;
    }


    @Override
    protected Object invoke(Object[] args) {
        return MethodUtils.invoke(parent.getBean(), method, args);
    }

    @Override
    public boolean isReadyForProduction() {
        if (parent.getBean() == null) return false;
        return super.isReadyForProduction();
    }

    Class<?> getReturnType() {
        return method.getReturnType();
    }
}
