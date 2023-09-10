package one.xis.context;

import lombok.Getter;

@Getter
class SimpleParameter implements ComponentParameter {

    private final Class<?> type;
    private final int index;
    private Object value;


    SimpleParameter(Class<?> type, int index) {
        this.type = type;
        this.index = index;
    }

    @Override
    public void onComponentCreated(Object o) {
        if (type.isInstance(o)) {
            value = o;

        }
    }

    @Override
    public boolean isComplete() {
        return value != null;
    }


}
