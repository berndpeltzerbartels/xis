package one.xis.context;

import java.util.Set;

public interface SingletonInstantiator<T> {
    
    void onComponentCreated(Object o);

    boolean isParameterCompleted();

    T createInstance();

    /**
     * Enables contrucotr parameters of singletons to
     * know their candidates
     *
     * @param singletonClasses all singleton classes
     */
    void onSingletonClassesFound(Set<Class<?>> singletonClasses);

    Class<?> getType();
}
