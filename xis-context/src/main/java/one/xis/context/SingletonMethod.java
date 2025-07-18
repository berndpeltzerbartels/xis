package one.xis.context;

import lombok.Getter;
import one.xis.utils.lang.MethodUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

@Getter
class SingletonMethod extends SingletonProducerImpl {
    private final Method method;
    private final SingletonWrapper parent;

    @Getter
    private boolean invoked;

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
    protected Object invoke(Object[] args) {
        invoked = true;
        try {
            return MethodUtils.invoke(parent.getBean(), method, args);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean isInvocable() {
        if (parent.getBean() == null) return false;
        if (!getParent().getSingletonFields().isEmpty()) return false;
        return super.isInvocable();
    }

    boolean isFinalizable() {
        if (parent.getBean() == null) return false;
        if (getParent().getSingletonFields().stream().anyMatch(field -> !field.isOptional())) return false;
        return super.isInvocable();
    }

    Class<?> getReturnType() {
        return method.getReturnType();
    }
}
