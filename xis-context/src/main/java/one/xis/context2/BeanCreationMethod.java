package one.xis.context2;

import java.lang.reflect.Method;

class BeanCreationMethod extends SingletonMethod {
    BeanCreationMethod(Method method, SingletonWrapper parent, ParameterFactory parameterFactory) {
        super(method, parent, parameterFactory);
    }

    @Override
    public void invoke() {
        getParent().removeBeanMethod(this);
        super.invoke();
    }

    @Override
    public boolean isInvocable() {
        if (getParent().getBean() == null) return false;
        if (!getParent().getSingletonFields().isEmpty()) return false;
        if (!getParent().getInitMethods().isEmpty()) return false;
        return super.isInvocable();
    }
}
