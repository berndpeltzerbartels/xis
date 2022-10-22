package one.xis.invocation;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
class ModelParameter implements MethodParameter {
    private final String id;
    private final Class<?> modelType;

}
