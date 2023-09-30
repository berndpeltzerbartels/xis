package one.xis.context.beanmethod;

import one.xis.context.XISBean;
import one.xis.context.XISComponent;
import one.xis.context.XISInject;

@XISComponent
class Comp1 {

    @XISInject
    private Comp2 comp2;

    @XISBean
    Comp3 comp3() {
        return new Comp3(comp2);
    }

}
