package one.xis.context.defaultcomponent.multiple;

import one.xis.context.Component;
import one.xis.context.DefaultComponent;

@Component
@DefaultComponent
public class DefaultHandler implements Handler {
    @Override
    public void handle() {
        // default implementation
    }
}
