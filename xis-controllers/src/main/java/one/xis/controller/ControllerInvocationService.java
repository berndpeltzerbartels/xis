package one.xis.controller;

import lombok.NonNull;
import one.xis.context.XISComponent;
import one.xis.dto.ActionRequest;
import one.xis.dto.ActionResponse;
import one.xis.dto.InitialRequest;
import one.xis.dto.InitialResponse;

@XISComponent
public class ControllerInvocationService {

    public InitialResponse invokeInitial(@NonNull Object controller, @NonNull InitialRequest request) {
        var invoker = new ControllerInitializeInvoker(controller, request);
        invoker.invokeInitial();
        var response = new InitialResponse();
        response.setClientState(invoker.getClientState());
        response.setComponentState(invoker.getComponentState());
        return response;
    }

    public ActionResponse invokeForAction(@NonNull Object controller, @NonNull ActionRequest request, @NonNull String action, @NonNull String javascriptClassName) {
        var invoker = new ControllerActionInvoker(controller, request);
        var nextControllerClass = invoker.invokeForAction(action);
        var response = new ActionResponse();
        response.setClientState(invoker.getClientState());
        response.setComponentState(invoker.getComponentState());
        response.setNextController(nextControllerClass);
        response.setNextComponent(javascriptClassName);
        return response;
    }
}
