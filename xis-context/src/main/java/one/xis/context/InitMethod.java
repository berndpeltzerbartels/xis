package one.xis.context;

import org.tinylog.Logger;

import java.lang.reflect.Method;

class InitMethod extends SingletonMethod {
    InitMethod(Method method, SingletonWrapper parent, ParameterFactory parameterFactory) {
        super(method, parent, parameterFactory);
    }

    @Override
    public void invoke() {
        if (Logger.isDebugEnabled()) {
            Logger.debug("invoking init method {} of {}", getMethod().getName(), getParent().getBeanClass().getName());
        }
        super.invoke();
    }

}
