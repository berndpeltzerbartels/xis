package one.xis.context.collectionfieldinject;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import one.xis.context.XISComponent;
import one.xis.context.XISInject;

import java.util.Collection;

@XISComponent
@Getter
@RequiredArgsConstructor
class Comp1 {
    @XISInject
    private Collection<Interf1> field1;

    @XISInject
    private Collection<? extends Interf1> field2;
}
