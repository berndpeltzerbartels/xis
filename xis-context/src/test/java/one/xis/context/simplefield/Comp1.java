package one.xis.context.simplefield;


import lombok.Getter;
import one.xis.context.Component;
import one.xis.context.XISInject;

@Component
@Getter
class Comp1 {
    @XISInject
    private Comp2 comp2;
}
