package one.xis.context.defaultcomponent.explicit;

import one.xis.context.Component;
import one.xis.context.DefaultComponent;
import one.xis.context.defaultcomponent.fallback.Processor;

@Component
@DefaultComponent
public class ExplicitDefaultProcessor implements Processor {
    @Override
    public void process() {
        // explicitly registered default implementation
    }
}
