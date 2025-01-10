package one.xis.context2;

import java.lang.reflect.Constructor;
import java.util.Collection;
import java.util.List;

public class SingletonConstructor extends SingletonProducerImpl {


    private final Constructor<?> constructor;

    SingletonConstructor(Constructor<?> constructor, ParameterFactory parameterFactory) {
        super(constructor.getParameters(), parameterFactory);
        this.constructor = constructor;
    }


    @Override
    protected Object invoke(Object[] args) {
        return null;
    }

    @Override
    public Class<?> getSingletonClass() {
        return constructor.getDeclaringClass();
    }


    @Override
    public void invoke() {
        var o = invoke(getArgs());
        notifyConsumers(o);
        notifySingletonCreationListeners(o);
    }

    @Override
    public void assignValue(Object o) {

    }

    @Override
    public boolean isConsumerFor(Class<?> c) {
        return false;
    }


    @Override
    public Collection<Class<?>> getUnsatisfiedDependencies() {
        return List.of();
    }

    @Override
    public SingletonProducer getProducer() {
        return null;
    }

    @Override
    public void setProducer(SingletonProducer producer) {

    }

}
