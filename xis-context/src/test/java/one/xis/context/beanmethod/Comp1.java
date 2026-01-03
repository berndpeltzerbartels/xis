package one.xis.context.beanmethod;

import lombok.Getter;
import one.xis.context.Bean;
import one.xis.context.Component;
import one.xis.context.Inject;

@Getter
@Component
class Comp1 {

    @Inject
    private Comp2 comp2;

    @Bean
    Comp3 comp3() {
        return new Comp3(comp2);
    }

}
