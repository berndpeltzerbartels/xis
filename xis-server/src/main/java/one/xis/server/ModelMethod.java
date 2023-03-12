package one.xis.server;

import lombok.SneakyThrows;
import lombok.experimental.SuperBuilder;

@SuperBuilder
class ModelMethod extends ControllerMethod {

    @Override
    @SneakyThrows
    Object invoke(InvocationContext context) {
        return method.invoke(controller, prepareArgs(context));
    }

    @Override
    InvocationType getInvocationType() {
        return InvocationType.ACTION;
    }
}
