package one.xis.controller;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
class ModelParameter implements MethodParameter {
    private final String id;
    private final Class<?> modelType;

}
