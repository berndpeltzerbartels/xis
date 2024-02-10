package one.xis.server;

import lombok.experimental.SuperBuilder;

import java.lang.reflect.Method;
import java.util.Map;

@SuperBuilder
class ActionMethod extends ControllerMethod {


    @Override
    protected Object[] prepareArgs(Method method, ClientRequest context, Map<String, Throwable> errors) throws Exception {
        var args = parameterPreparation.prepareParameters(method, context, errors);

        return args;
    }
}
