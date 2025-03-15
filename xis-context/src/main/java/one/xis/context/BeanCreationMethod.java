package one.xis.context;

import org.tinylog.Logger;

import java.lang.reflect.Method;

class BeanCreationMethod extends SingletonMethod {
    BeanCreationMethod(Method method, SingletonWrapper parent, ParameterFactory parameterFactory) {
        super(method, parent, parameterFactory);
    }

    @Override
    public void invoke() {
        if (Logger.isDebugEnabled()) {
            Logger.debug("invoking bean method {} of {}", getMethod().getName(), getParent().getBeanClass().getName());
        }
        super.invoke();
    }

    @Override
    public boolean isInvocable() {
        if (getParent().getBean() == null) return false;
        if (!getParent().getSingletonFields().isEmpty()) return false;
        if (!getParent().getInitMethods().isEmpty()) return false;
        return super.isInvocable();
    }

    @Override
    public Class<?> getSingletonClass() {
        return getMethod().getReturnType();
    }

    @Override
    public String toString() {
        return "BeanCreationMethod{" + getMethod().getDeclaringClass().getSimpleName() + "." + getMethod().getName() + "}";
    }
}
