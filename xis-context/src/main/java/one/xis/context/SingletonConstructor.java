package one.xis.context;

import lombok.Getter;
import lombok.NonNull;

import java.lang.reflect.Constructor;

public class SingletonConstructor extends SingletonProducerImpl {


    private final Constructor<?> constructor;

    @Getter
    private boolean invoked;

    SingletonConstructor(@NonNull Constructor<?> constructor, @NonNull ParameterFactory parameterFactory) {
        super(constructor.getParameters(), parameterFactory);
        this.constructor = constructor;
    }

    @Override
    protected Object invoke(Object[] args) {
        invoked = true;
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
    public String toString() {
        return "SingletonConstructor{" +
                constructor.getDeclaringClass().getSimpleName() +
                '}';
    }
}
