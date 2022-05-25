package one.xis.context.arrayfield;

import one.xis.context.AppContextInitializer;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static one.xis.utils.lang.CollectionUtils.elementOfClass;
import static org.assertj.core.api.Assertions.assertThat;

class ArrayFieldITCase {


    @Test
    void arrayField() {

        AppContextInitializer initializer = new AppContextInitializer(getClass());
        initializer.run();

        Set<Object> singletons = initializer.getSingletons();

        Comp1 comp1 = elementOfClass(singletons, Comp1.class);
        Comp2 comp2 = elementOfClass(singletons, Comp2.class);
        Comp3 comp3 = elementOfClass(singletons, Comp3.class);

        assertThat(comp1).isNotNull();
        assertThat(comp2).isNotNull();
        assertThat(comp3).isNotNull();

        assertThat(comp1.getField1()).containsExactlyInAnyOrder(comp2, comp3);
        assertThat(comp1.getField2()).containsExactlyInAnyOrder(comp2, comp3);

    }
}
