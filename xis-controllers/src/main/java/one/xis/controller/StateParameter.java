package one.xis.controller;

import lombok.Data;
import one.xis.dto.Request;

@Data
class StateParameter implements MethodParameter<Object> {
    private final String name;
    private final String key;
    private Class<?> type;
    private Object value;

    @Override
    public Object valueFromRequest(Request request) {
        value = request.getClientState().computeIfAbsent(key, k -> this.createInstance());
        return value;
    }

    private Object createInstance() {
        return null;
    }
}
