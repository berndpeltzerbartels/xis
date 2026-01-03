package one.xis.context.performance.p9;

import lombok.RequiredArgsConstructor;
import one.xis.context.XISBean;
import one.xis.context.Component;
import one.xis.context.Init;
import one.xis.context.performance.XI;

@Component
@RequiredArgsConstructor
class C2 implements XI {

    private final C9 c9;

    @XISBean
    C4 c4() {
        return new C4(c9);
    }

    @Init
    void init0() {

    }

    @Init
    void init1() {

    }

    @Init
    void init2() {

    }

    @Init
    void init3() {

    }

    @Init
    void init4() {

    }
}
