package one.xis.server;

import lombok.experimental.SuperBuilder;

@SuperBuilder
class ActionMethod extends ControllerMethod {


    @Override
    protected Object[] prepareArgs(ClientRequest context) throws Exception {
        var args = super.prepareArgs(context);

        return args;
    }
}
