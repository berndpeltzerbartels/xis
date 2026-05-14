package one.xis.context;

import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;

@Slf4j
class InitMethod extends SingletonMethod {
    InitMethod(Method method, SingletonWrapper parent, ParameterFactory parameterFactory) {
        super(method, parent, parameterFactory);
    }

    @Override
    public void invoke() {
        if (log.isDebugEnabled()) {
            log.debug("invoking init method {} of {}", getMethod().getName(), getParent().getBeanClass().getName());
        }
        super.invoke();
    }

}
