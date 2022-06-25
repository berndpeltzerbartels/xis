package one.xis.context.simplefield;


import lombok.Getter;
import one.xis.context.XISComponent;
import one.xis.context.XISInject;

@XISComponent
@Getter
class Comp1 {
    @XISInject
    private Comp2 comp2;
}
