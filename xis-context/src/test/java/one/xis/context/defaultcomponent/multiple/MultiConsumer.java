package one.xis.context.defaultcomponent.multiple;

import lombok.Getter;
import one.xis.context.Component;
import one.xis.context.Inject;

import java.util.Collection;

@Component
@Getter
public class MultiConsumer {
    @Inject
    private Collection<Handler> handlers;
}
