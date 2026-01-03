package one.xis.context.beanmethod;

import lombok.Getter;
import one.xis.context.XISBean;
import one.xis.context.Component;
import one.xis.context.XISInject;

@Getter
@Component
class Comp1 {

    @XISInject
    private Comp2 comp2;

    @XISBean
    Comp3 comp3() {
        return new Comp3(comp2);
    }

}
