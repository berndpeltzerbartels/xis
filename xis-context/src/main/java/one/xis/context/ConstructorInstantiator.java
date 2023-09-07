package one.xis.context;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * Instantiates a singleton. Each singleton-type has an instance of {@link ConstructorInstantiator}.
 */
@RequiredArgsConstructor
class ConstructorInstantiator implements SingletonInstantiator<Object> {
    @Getter
    private final Class<?> type;

    @Getter
    private List<ConstructorParameter> constructorParameters;
    private Constructor<?> constructor;
    private AtomicInteger missingConstructorParameters;

    void init() {
        constructor = getConstructor();
        constructorParameters = Arrays.stream(constructor.getParameters()).map(ConstructorParameter::create).collect(Collectors.toList());
        missingConstructorParameters = new AtomicInteger(constructorParameters.size());
    }

    @Override
    public void onComponentCreated(Object o) {
        setConstructorParameters(o);
    }

    private void setConstructorParameters(Object o) {
        for (ConstructorParameter constructorParameter : constructorParameters) {
            if (!constructorParameter.isComplete()) {
                constructorParameter.onComponentCreated(o);
                if (constructorParameter.isComplete()) {
                    missingConstructorParameters.decrementAndGet();
                }
            }
        }
    }

    @Override
    public boolean isParameterCompleted() {
        return missingConstructorParameters.get() < 1;
    }

    private Constructor<?> getConstructor() {
        List<Constructor<?>> constructors = Arrays.stream(type.getDeclaredConstructors()).filter(this::nonPrivate).collect(Collectors.toList());
        switch (constructors.size()) {
            case 0:
                throw new AppContextException("no accessible constructor for " + type);
            case 1:
                return constructors.get(0);
            default:
                throw new AppContextException("too many constructors for " + type);
        }
    }

    private boolean nonPrivate(Executable accessibleObject) {
        return !Modifier.isPrivate(accessibleObject.getModifiers());
    }

    @Override
    @SneakyThrows
    public Object createInstance() {
        constructor.setAccessible(true);
        try {
            return constructor.newInstance(getParameterValues());
        } catch (Exception e) {
            throw new AppContextException("failed to create instance of " + constructor.getDeclaringClass(), e);
        }
    }

    private Object[] getParameterValues() {
        return constructorParameters.stream().map(ConstructorParameter::getValue).toArray();
    }

    @Override
    public void onSingletonClassesFound(Set<Class<?>> singletonClasses) {
        constructorParameters.stream().filter(MultiValueParameter.class::isInstance)
                .map(MultiValueParameter.class::cast)
                .forEach(parameter -> parameter.registerSingletonClasses(singletonClasses));
    }
}
