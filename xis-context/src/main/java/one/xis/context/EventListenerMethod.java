package one.xis.context;

import lombok.Data;
import one.xis.utils.lang.MethodUtils;

import java.lang.reflect.Method;

@Data
class EventListenerMethod {
    private final SingletonWrapper singletonWrapper;
    private final Method method;

    boolean matches(Object eventData) {
        Class<?>[] parameterTypes = method.getParameterTypes();
        if (parameterTypes.length != 1) {
            throw new IllegalStateException("Event listener method must have exactly one parameter, but found: " + parameterTypes.length);
        }
        return parameterTypes[0].isAssignableFrom(eventData.getClass());
    }

    void invoke(Object eventData) {
        try {
            MethodUtils.invoke(singletonWrapper.getBean(), method, eventData);
        } catch (Exception e) {
            throw new RuntimeException("Failed to invoke event listener method: " + method, e);
        }
    }
}
