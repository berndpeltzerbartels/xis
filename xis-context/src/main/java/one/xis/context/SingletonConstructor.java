package one.xis.context;

import java.lang.reflect.Constructor;

public class SingletonConstructor extends SingletonProducerImpl {


    private final Constructor<?> constructor;

    SingletonConstructor(Constructor<?> constructor, ParameterFactory parameterFactory) {
        super(constructor.getParameters(), parameterFactory);
        this.constructor = constructor;
    }

    @Override
    protected Object invoke(Object[] args) {
        try {
            constructor.setAccessible(true);
            return constructor.newInstance(args);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Class<?> getSingletonClass() {
        return constructor.getDeclaringClass();
    }

    @Override
    public void invoke() {
        var o = invoke(getArgs());
        assignValueInConsumers(o);
        notifySingletonCreationListeners(o);
    }

    @Override
    public String toString() {
        return "SingletonConstructor{" +
                constructor.getDeclaringClass().getSimpleName() +
                '}';
    }
}
