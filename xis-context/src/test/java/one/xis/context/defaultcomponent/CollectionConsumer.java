package one.xis.context.defaultcomponent;

import lombok.Getter;
import one.xis.context.Component;
import one.xis.context.Inject;

import java.util.Collection;

@Component
@Getter
public class CollectionConsumer {
    @Inject
    private Collection<Service> services;
}
