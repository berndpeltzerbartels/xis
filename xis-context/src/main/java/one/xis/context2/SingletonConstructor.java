package one.xis.context2;

import java.lang.reflect.Constructor;

public class SingletonConstructor extends SingletonProducerImpl {


    private final Constructor<?> constructor;

    SingletonConstructor(Constructor<?> constructor) {
        super(constructor.getParameters());
        this.constructor = constructor;
    }
}
