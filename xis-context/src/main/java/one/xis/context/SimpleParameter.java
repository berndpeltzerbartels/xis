package one.xis.context;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
class SimpleParameter extends ConstructorParameter {

    private final Class<?> type;

    @Getter
    private Object value;

    @Override
    public boolean onComponentCreated(Object o) {
        if (type.isInstance(o)) {
            value = o;
            return true;
        }
        return false;
    }

}
