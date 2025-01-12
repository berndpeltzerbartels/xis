package one.xis.context2;

import java.lang.reflect.Method;

class InitMethod extends SingletonMethod {
    InitMethod(Method method, SingletonWrapper parent, ParameterFactory parameterFactory) {
        super(method, parent, parameterFactory);
    }

    @Override
    public void invoke() {
        getParent().removeInitMethod(this);
        super.invoke();
    }

}
