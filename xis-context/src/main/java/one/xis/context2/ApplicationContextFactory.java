package one.xis.context2;

import lombok.RequiredArgsConstructor;
import one.xis.utils.lang.MethodUtils;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

@RequiredArgsConstructor
class ApplicationContextFactory implements SingletonCreationListener {
    private final Set<SingletonProducer> singletonProducers = new HashSet<>();
    private final Set<Object> singletons = new HashSet<>();
    private final Parameters parameters = new Parameters();
    private final Fields fields = new Fields();
    private final Annotations annotations;

    private final Class<?>[] annotatedComponentClasses;

    void init() {
        for (var i = 0; i < annotatedComponentClasses.length; i++) {
            createProducers(annotatedComponentClasses[i]);
        }
    }

    private void createProducers(Class<?> annotatedComponentClass) {
        try {
            singletonProducers.add(new SingletonConstructor(annotatedComponentClass.getConstructor()));
            addBeanTypesByFactoryMethods(annotatedComponentClass);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }


    private void mapProducers() {
        var unmappedListeners = new HashSet<>(singletonProducers);
        var unmappedProducers2 = new HashSet<>(singletonProducers);
        while (!unmappedListeners.isEmpty()) {

        }
    }

    private void addBeanTypesByFactoryMethods(Class<?> c) {
        MethodUtils.methods(c, this::isBeanMethod).forEach(method -> {
            singletonProducers.add(new SingletonMethod(method, creationListener));
            addBeanTypesByFactoryMethods(method.getReturnType());
        });
    }

    private boolean isBeanMethod(Method method) {
        return annotations.isAnnotatedMethod(method);
    }


    @Override
    public void onSingletonCreated(Object o) {

    }
}
