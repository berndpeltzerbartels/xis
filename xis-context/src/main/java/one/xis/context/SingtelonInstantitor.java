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
import java.util.stream.Collectors;

/**
 * Instantiates a singleton. Each singelton-type has an instance of {@link SingtelonInstantitor}.
 */
@RequiredArgsConstructor
class SingtelonInstantitor {
    @Getter
    private final Class<?> type;
    private List<ConstructorParameter> constructorParameters;
    private Constructor<?> constructor;
    private int missingConstructorParameters;

    void init() {
        constructor = getConstructor();
        constructorParameters = Arrays.stream(constructor.getParameters()).map(ConstructorParameter::create).collect(Collectors.toList());
        missingConstructorParameters = constructorParameters.size();
    }

    void onComponentCreated(Object o) {
        setConstructorParameters(o);
    }

    private void setConstructorParameters(Object o) {
        for (ConstructorParameter constructorParameter : constructorParameters) {
            if (!constructorParameter.isComplete()) {
                constructorParameter.onComponentCreated(o);
                if (constructorParameter.isComplete()) {
                    missingConstructorParameters--;
                }
            }
        }
    }

    boolean isParameterCompleted() {
        return missingConstructorParameters < 1;
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

    @SneakyThrows
    Object createInstance() {
        constructor.setAccessible(true);
        return constructor.newInstance(getParameterValues());
    }

    private Object[] getParameterValues() {
        return constructorParameters.stream().map(ConstructorParameter::getValue).toArray();
    }

    void registerSingletonClasses(Set<Class<?>> singletonClasses) {
        constructorParameters.stream().filter(MultiValueParameter.class::isInstance)
                .map(MultiValueParameter.class::cast)
                .forEach(parameter -> parameter.registerSingletonClasses(singletonClasses));
    }
}
