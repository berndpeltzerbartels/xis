package one.xis.invocation;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import one.xis.Request;

@Getter
@RequiredArgsConstructor
class ModelParameter implements MethodParameter {
    private final String id;
    private final Class<?> modelType;

    @Override
    public void setValue(Request request) {

    }

}
