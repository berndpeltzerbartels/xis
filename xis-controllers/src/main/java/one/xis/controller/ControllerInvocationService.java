package one.xis.controller;

import one.xis.context.XISComponent;
import one.xis.dto.Request;
import one.xis.dto.Response;

@XISComponent
public class ControllerInvocationService {

    public Response invokeInitial(Object controller, Request request) {
        var invoker = new ControllerInitializeInvoker(controller, request);
        invoker.invokeInitial();
        var response = new Response();
        response.setClientState(invoker.getClientState());
        response.setComponentModel(invoker.getComponentModel());
        return response;
    }

    public Response invokeForAction(Object controller, Request request, String action) {
        var invoker = new ControllerActionInvoker(controller, request);
        invoker.invokeForAction(action);
        var response = new Response();
        response.setClientState(invoker.getClientState());
        response.setComponentModel(invoker.getComponentModel());
        return response;
    }
}
