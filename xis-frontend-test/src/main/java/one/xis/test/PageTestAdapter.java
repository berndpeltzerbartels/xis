package one.xis.test;

import one.xis.context.AppContext;


class PageTestAdapter {

    private final Class<?> controllerClass;
    private final AppContext appContext;
    private final Object controller;

    PageTestAdapter(Class<?> controllerClass, AppContext appContext) {
        this.controllerClass = controllerClass;
        this.appContext = appContext;
        this.controller = appContext.getSingleton(controllerClass);
    }

    private void init() {


    }
}
