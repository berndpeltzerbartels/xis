package one.xis.context2;

import lombok.Getter;
import one.xis.utils.lang.MethodUtils;

import java.lang.reflect.Method;

@Getter
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
    protected Object invoke(Object[] args) {
        return MethodUtils.invoke(parent.getBean(), method, args);
    }

    @Override
    public boolean isInvocable() {
        if (parent.getBean() == null) return false;
        return super.isInvocable();
    }

    Class<?> getReturnType() {
        return method.getReturnType();
    }
}
