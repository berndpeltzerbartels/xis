package one.xis.server;

import lombok.experimental.SuperBuilder;

@SuperBuilder
class ActionMethod extends ControllerMethod {
    
    @Override
    InvocationType getInvocationType() {
        return InvocationType.ACTION;
    }
}
