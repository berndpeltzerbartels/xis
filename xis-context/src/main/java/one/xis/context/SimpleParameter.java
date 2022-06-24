package one.xis.context;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
class SimpleParameter extends ConstructorParameter {

    private final Class<?> type;

    @Getter
    private Object value;

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
}
