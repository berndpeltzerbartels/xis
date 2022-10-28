package one.xis.controller;

import lombok.Data;

@Data
public class ControllerWrapper {
    private final Object contoller;
    private final Class<?> modelType;
}
