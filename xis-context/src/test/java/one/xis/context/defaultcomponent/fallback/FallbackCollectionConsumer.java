package one.xis.context.defaultcomponent.fallback;

import lombok.Getter;
import one.xis.context.Component;
import one.xis.context.Inject;

import java.util.Collection;

@Component
@Getter
public class FallbackCollectionConsumer {
    @Inject
    private Collection<Processor> processors;
}
