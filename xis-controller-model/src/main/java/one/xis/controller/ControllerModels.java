package one.xis.controller;

import lombok.RequiredArgsConstructor;
import one.xis.context.XISComponent;
import one.xis.context.XISInit;

import java.util.Collection;

@XISComponent
@RequiredArgsConstructor
class ControllerModels {

    private final ControllerModelFactory controllerModelFactory;

    private Collection<ControllerModel> widgetControllers;
    private Collection<ControllerModel> pageControllers;

    @XISInit
    void init() {
        
    }
}
