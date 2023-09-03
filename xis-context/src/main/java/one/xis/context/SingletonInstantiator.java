package one.xis.context;

import lombok.SneakyThrows;

public interface SingletonInstantiator {
    void onComponentCreated(Object o);

    boolean isParameterCompleted();

    @SneakyThrows
    Object createInstance();
}
