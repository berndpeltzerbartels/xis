package one.xis.context.initmethod;


import lombok.Getter;
import lombok.RequiredArgsConstructor;
import one.xis.context.Component;
import one.xis.context.Init;
import one.xis.context.Inject;

@Component
@Getter
@RequiredArgsConstructor
class Comp1 {
    private final Comp3 comp3;

    @Inject
    private Comp2 comp2;

    private int result;

    @Init
    void init() {
        result = comp2.getInt() + comp3.getInt();
    }
}
