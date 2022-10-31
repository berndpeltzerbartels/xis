package one.xis.controller;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import one.xis.dto.Request;

@Getter
@RequiredArgsConstructor
class ModelParameter implements MethodParameter<Object> {
    private final String id;
    private final Class<?> modelType;
    private final String paramName;
    private Object value;

    @Override
    public Object valueFromRequest(Request request) {
        value = request.getComponentModel();
        return value;
    }
}
