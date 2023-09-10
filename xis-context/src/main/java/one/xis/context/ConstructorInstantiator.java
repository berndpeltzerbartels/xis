package one.xis.context;

import java.lang.reflect.Constructor;
import java.util.Queue;
import java.util.function.Consumer;

class ConstructorInstantiator implements Instantiator {
    private final Constructor<?> constructor;
    private final Queue<ComponentParameter> parameters;
    private final Object[] args;
    private final Consumer<Object> objectConsumer;

    ConstructorInstantiator(Constructor<?> constructor, Queue<ComponentParameter> parameters, Consumer<Object> objectConsumer) {
        this.constructor = constructor;
        this.parameters = parameters;
        this.objectConsumer = objectConsumer;
        args = new Object[parameters.size()];
        constructor.setAccessible(true);
    }


    @Override
    public boolean isExecutable() {
        return parameters.isEmpty();
    }

    @Override
    public void createInstance() {
        try {
            var o = constructor.newInstance(args);
            objectConsumer.accept(o);
        } catch (Exception e) {
            throw new RuntimeException("constructor failed", e);
        }
    }

    @Override
    public void onComponentCreated(Object o) {
        for (var param : parameters) {
            param.onComponentCreated(o);
            if (param.isComplete()) {
                parameters.remove(param);
                args[param.getIndex()] = param.getValue();
            }
        }
    }
}
