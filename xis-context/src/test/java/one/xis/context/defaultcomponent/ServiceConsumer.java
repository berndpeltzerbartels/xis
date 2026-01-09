package one.xis.context.defaultcomponent;

import lombok.Getter;
import one.xis.context.Component;
import one.xis.context.Inject;

@Component
@Getter
public class ServiceConsumer {
    @Inject
    private Service service;
}
