package one.xis.context.singletonwithcollection;

import lombok.RequiredArgsConstructor;
import one.xis.context.Component;

import java.util.Collection;

@Component
@RequiredArgsConstructor
class ConsumerWithCollection {
    private final Collection<MyInterface> instances;

    public Collection<MyInterface> getInstances() {
        return instances;
    }
}
