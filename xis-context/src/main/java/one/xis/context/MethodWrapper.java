package one.xis.context;

import lombok.Getter;
import lombok.Setter;
import one.xis.utils.lang.MethodUtils;

import java.lang.reflect.Method;
import java.util.Queue;
import java.util.function.Consumer;


class MethodWrapper {

    private final Object owner;
    private final Method method;
    private final Queue<ComponentParameter> componentParameters;
    private final Consumer<Object> componentConsumer;
    private final Object[] args;

    @Getter
    @Setter
    private ComponentWrapper componentWrapper;

    MethodWrapper(Object owner, Method method, Queue<ComponentParameter> componentParameters, Consumer<Object> componentConsumer) {
        this.owner = owner;
        this.method = method;
        this.componentParameters = componentParameters;
        this.componentConsumer = componentConsumer;
        this.args = new Object[componentParameters.size()];
    }

    void execute() {
        var result = MethodUtils.invoke(owner, method, args);
        if (isVoid(method.getReturnType())) {
            componentConsumer.accept(result);
        }
    }

    void onComponentCreated(Object o) {
        for (var param : componentParameters) {
            param.onComponentCreated(o);
            if (param.isComplete()) {
                componentParameters.remove(param);
            }
        }
    }

    boolean isExecutable() {
        return componentParameters.isEmpty();
    }

    private static boolean isVoid(Class<?> c) {
        return c == Void.class || c == Void.TYPE;
    }
}
