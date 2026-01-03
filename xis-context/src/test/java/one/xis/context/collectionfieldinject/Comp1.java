package one.xis.context.collectionfieldinject;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import one.xis.context.Component;
import one.xis.context.Inject;

import java.util.Collection;

@Component
@Getter
@RequiredArgsConstructor
class Comp1 {
    @Inject
    private Collection<Interf1> field1;

    @Inject
    private Collection<? extends Interf1> field2;
}
