package one.xis.context.defaultcomponent;

import one.xis.context.Component;
import one.xis.context.DefaultComponent;

@Component
@DefaultComponent
public class DefaultServiceImpl implements Service {
    @Override
    public String getName() {
        return "default";
    }
}
