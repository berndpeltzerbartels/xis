package one.xis.context.simplefield;

import one.xis.context.AppContext;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static one.xis.utils.lang.CollectionUtils.findElementOfType;
import static org.assertj.core.api.Assertions.assertThat;

class SimpleFieldITCase {

    @Test
    void simpleFieldTest() {

        Set<Object> singletons = AppContext.getInstance(getClass()).getSingletons();

        Comp1 comp1 = findElementOfType(singletons, Comp1.class);
        Comp2 comp2 = findElementOfType(singletons, Comp2.class);
        Comp3 comp3 = findElementOfType(singletons, Comp3.class);

        assertThat(comp1).isNotNull();
        assertThat(comp2).isNotNull();
        assertThat(comp3).isNotNull();

        assertThat(comp1.getComp2()).isNotNull();
        assertThat(comp2.getComp3()).isNotNull();

    }

}
