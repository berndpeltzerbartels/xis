package one.xis.invoke;

import one.xis.context.AppContext;

public class TestInvoker {

    private final Class<?> controllerClass;
    private final AppContext appContext;
    private final Object controller;

    public TestInvoker(Class<?> controllerClass, AppContext appContext) {
        this.controllerClass = controllerClass;
        this.appContext = appContext;
        this.controller = appContext.getSingleton(controllerClass);
    }

  
}
