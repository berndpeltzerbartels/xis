package one.xis.context.defaultcomponent.fallback;

import lombok.Getter;
import one.xis.context.Component;
import one.xis.context.Inject;

@Component
@Getter
public class FallbackConsumer {
    @Inject
    private Processor processor;
}
