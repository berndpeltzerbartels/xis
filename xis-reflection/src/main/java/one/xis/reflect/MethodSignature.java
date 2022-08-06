package one.xis.reflect;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import one.xis.utils.lang.MethodUtils;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Getter
@EqualsAndHashCode
@RequiredArgsConstructor
class MethodSignature {
    private final String name;
    private final List<Class<?>> parameterTypes;
    private final String signature;
    private final int modifiers;

    public static MethodSignature from(Method method) {
        return new MethodSignature(method.getName(), parameterTypes(method), MethodUtils.methodSignature(method), method.getModifiers());
    }

    private static List<Class<?>> parameterTypes(Method method) {
        return Arrays.stream(method.getParameters()).map(Parameter::getType).collect(Collectors.toUnmodifiableList());
    }

    public boolean matches(Method method) {
        if (!method.getName().equals(name)) {
            return false;
        }
        if (method.getParameters().length != parameterTypes.size()) {
            return false;
        }
        for (int i = 0; i < parameterTypes.size(); i++) {
            if (method.getParameters()[i].getType().equals(parameterTypes.get(i))) {
                return false;
            }
        }
        if (method.getModifiers() != modifiers) {
            return false;
        }
        return true;
    }

    public Optional<Method> getMethodWithSignature(Object base) {
        return MethodUtils.methods(base).stream().filter(this::matches).findFirst();
    }

    @Override
    public String toString() {
        return signature;
    }
}
