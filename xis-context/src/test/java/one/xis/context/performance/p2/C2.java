package one.xis.context.performance.p2;

import lombok.RequiredArgsConstructor;
import one.xis.context.Bean;
import one.xis.context.Component;
import one.xis.context.XISInit;
import one.xis.context.performance.XI;

@Component
@RequiredArgsConstructor
public class C2 implements XI {

    private final C9 c9;

    @Bean
    C4 c4() {
        return new C4(c9);
    }

    @XISInit
    void init0() {

    }

    @XISInit
    void init1() {

    }

    @XISInit
    void init2() {

    }

    @XISInit
    void init3() {

    }

    @XISInit
    void init4() {

    }
}
