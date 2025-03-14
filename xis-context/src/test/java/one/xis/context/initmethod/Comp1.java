package one.xis.context.initmethod;


import lombok.Getter;
import lombok.RequiredArgsConstructor;
import one.xis.context.XISComponent;
import one.xis.context.XISInit;
import one.xis.context.XISInject;

@XISComponent
@Getter
@RequiredArgsConstructor
class Comp1 {
    private final Comp3 comp3;

    @XISInject
    private Comp2 comp2;

    private int result;

    @XISInit
    void init() {
        result = comp2.getInt() + comp3.getInt();
    }
}
