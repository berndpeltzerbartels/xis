package one.xis.context;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
class Instantitor {
    @Getter
    private final Class<?> type;
    private List<Class<?>> parameterTypes;
    private Object[] parameters;
    private Constructor<?> constructor;
    private int missingParameters;

    void init() {
        constructor = checkConstructorParames(getConstructor());
        parameterTypes = List.of(constructor.getParameterTypes());
        parameters = new Object[parameterTypes.size()];
        missingParameters = parameterTypes.size();
    }

    void onComponentCreated(Object o) {
        setParameters(o);
    }

    private void setParameters(Object o) {
        for (int i = 0; i < parameterTypes.size(); i++) {
            if (parameterTypes.get(i).isInstance(o)) {
                if (parameters[i] != null) {
                    throw new AppContextException("ambigious candidates of type " + parameterTypes.get(i).getName() + " in constructor of " + type);
                }
                parameters[i] = o;
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

    private Constructor<?> checkConstructorParames(Constructor<?> constructor) {
        for (int i = 0; i < constructor.getParameterTypes().length; i++) {
            Class<?> clazz = constructor.getParameterTypes()[i];
            if (!clazz.isAnnotationPresent(Comp.class)) {
                throw new AppContextException(constructor + ": parameter nr " + (i + 1) + "is not annotated with " + Comp.class.getName());
            }
        }
        return constructor;
    }

    private boolean nonPrivate(Executable accessibleObject) {
        return !Modifier.isPrivate(accessibleObject.getModifiers());
    }

    @SneakyThrows
    Object createInstance() {
        constructor.setAccessible(true);
        return constructor.newInstance(parameters);
    }
}
