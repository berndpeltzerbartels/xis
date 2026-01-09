package one.xis.context.defaultcomponent;

import one.xis.context.Component;

@Component
public class CustomService implements Service {
    @Override
    public String getName() {
        return "custom";
    }
}
