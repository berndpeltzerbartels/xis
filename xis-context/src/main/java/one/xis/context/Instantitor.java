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

@RequiredArgsConstructor
class Instantitor {
    @Getter
    private final Class<?> type;
    private List<ConstructorParameter> parameters;
    private Constructor<?> constructor;
    private int missingParameters;

    void init() {
        constructor = getConstructor();
        parameters = Arrays.stream(constructor.getParameters()).map(ConstructorParameter::create).collect(Collectors.toList());
    }

    void onComponentCreated(Object o) {
        setParameters(o);
    }

    private void setParameters(Object o) {
        for (int i = 0; i < parameters.size(); i++) {
            if (parameters.get(i).onComponentCreated(o)) {
                missingParameters--;
            }
        }
    }

    boolean isParameterCompleted() {
        return missingParameters < 1;
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
        return parameters.stream().map(ConstructorParameter::getValue).toArray();
    }

    void populateSingletonClasses(Set<Class<?>> singletonClasses) {
        parameters.stream().filter(MultiValueParameter.class::isInstance)
                .map(MultiValueParameter.class::cast)
                .forEach(parameter -> parameter.populateSingletonClasses(singletonClasses));
    }
}
