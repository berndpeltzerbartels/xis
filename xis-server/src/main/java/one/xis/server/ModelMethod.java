package one.xis.server;

import lombok.experimental.SuperBuilder;

@SuperBuilder
class ModelMethod extends ControllerMethod {

    @Override
    InvocationType getInvocationType() {
        return InvocationType.MODEL;
    }
}
