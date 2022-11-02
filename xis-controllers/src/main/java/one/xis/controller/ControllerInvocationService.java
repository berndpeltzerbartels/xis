package one.xis.controller;

import one.xis.context.XISComponent;
import one.xis.dto.ActionResponse;
import one.xis.dto.InitialResponse;
import one.xis.dto.Request;

@XISComponent
public class ControllerInvocationService {

    public InitialResponse invokeInitial(Object controller, Request request) {
        var invoker = new ControllerInitializeInvoker(controller, request);
        invoker.invokeInitial();
        var response = new InitialResponse();
        response.setClientState(invoker.getClientState());
        response.setComponentModel(invoker.getComponentModel());
        return response;
    }

    public ActionResponse invokeForAction(Object controller, Request request, String action, String javascriptClassName) {
        var invoker = new ControllerActionInvoker(controller, request);
        var nextControllerClass = invoker.invokeForAction(action);
        var response = new ActionResponse();
        response.setClientState(invoker.getClientState());
        response.setComponentModel(invoker.getComponentModel());
        response.setNextController(nextControllerClass);
        response.setNextComponent(javascriptClassName);
        return response;
    }
}
