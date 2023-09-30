package one.xis.context.all;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import one.xis.context.XISBean;
import one.xis.context.XISComponent;
import one.xis.context.XISInject;

@Getter
@XISComponent
@RequiredArgsConstructor
class Comp1 {

    private final Comp3 comp3;

    @XISInject
    private Comp2 comp2;

    @XISBean
    Comp5 comp5(Comp4 comp4) {
        return new Comp5(comp4);
    }

}
