package one.xis.context.simplefield;


import lombok.Getter;
import one.xis.context.Component;
import one.xis.context.Inject;

@Component
@Getter
class Comp1 {
    @Inject
    private Comp2 comp2;
}
