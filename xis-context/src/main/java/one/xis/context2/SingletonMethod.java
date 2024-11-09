package one.xis.context2;

import java.lang.reflect.Method;

class SingletonMethod extends SingletonProducerImpl {
    private final Method method;
    private final Singleton parent;

    SingletonMethod(Method method, Singleton parent) {
        super(method.getParameters() );
        this.method = method;
        this.parent = parent;
    }


    @Override
    public Class<?> getSingletonClass() {
        return null;
    }

    public void execute() {
        var o =method.i
    }
}
