package one.xis.context2;

import java.lang.reflect.Method;

class BeanMethod extends SingletonMethod {
    BeanMethod(Method method, SingletonWrapper parent, ParameterFactory parameterFactory) {
        super(method, parent, parameterFactory);
    }

    @Override
    public void invoke() {
        getParent().removeBeanMethod(this);
        super.invoke();
    }

    @Override
    public boolean isInvocable() {
        if (!getParent().getInitMethods().isEmpty()) return false;
        return super.isInvocable();
    }
}
