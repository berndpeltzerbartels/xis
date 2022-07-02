package one.xis.context;

import lombok.Getter;

class SimpleParameter extends ConstructorParameter {

    private final Class<?> type;

    @Getter
    private Object value;

    SimpleParameter(Class<?> type, String name) {
        super(name);
        this.type = type;
    }

    @Override
    public void onComponentCreated(Object o) {
        if (type.isInstance(o)) {
            value = o;

        }
    }

    @Override
    boolean isComplete() {
        return value != null;
    }

    @Override
    Class<?> getElementType() {
        return type;
    }
}
