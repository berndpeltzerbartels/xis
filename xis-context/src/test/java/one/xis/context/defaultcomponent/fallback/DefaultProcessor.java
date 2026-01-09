package one.xis.context.defaultcomponent.fallback;

import one.xis.context.Component;
import one.xis.context.DefaultComponent;

@Component
@DefaultComponent
public class DefaultProcessor implements Processor {
    @Override
    public void process() {
        // default implementation
    }
}
