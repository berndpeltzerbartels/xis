package one.xis.context.simplefield;


import lombok.Getter;
import one.xis.context.Comp;
import one.xis.context.Inj;

@Comp
@Getter
class Comp1 {
    @Inj
    private Comp2 comp2;
}
