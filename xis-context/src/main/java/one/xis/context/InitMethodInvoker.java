package one.xis.context;

import lombok.RequiredArgsConstructor;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

@RequiredArgsConstructor
class InitMethodInvoker {
    Set<Object> owners = new HashSet<>();
    private final Method method;

    void onComponentCreated(Object o) {
        if (method.getDeclaringClass().isInstance(o)) {
            owners.add(o);
        }
    }

    void invoke() {
        owners.forEach(this::invoke);
    }

    private void invoke(Object owner) {
        try {
            method.setAccessible(true);
            method.invoke(owner);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new AppContextException(e);
        }
    }
}
