package one.xis.server;

import lombok.experimental.SuperBuilder;

import java.lang.reflect.Method;

@SuperBuilder
class ActionMethod extends ControllerMethod {


    @Override
    protected Object[] prepareArgs(Method method, ClientRequest context) throws Exception {
        var args = parameterPreparation.prepareParameters(method, context);

        return args;
    }
}
