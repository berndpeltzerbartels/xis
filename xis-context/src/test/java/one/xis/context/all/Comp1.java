package one.xis.context.all;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import one.xis.context.Bean;
import one.xis.context.Component;
import one.xis.context.Inject;

@Getter
@Component
@RequiredArgsConstructor
class Comp1 {

    private final Comp3 comp3;

    @Inject
    private Comp2 comp2;

    @Bean
    Comp5 comp5(Comp4 comp4) {
        return new Comp5(comp4);
    }

}
