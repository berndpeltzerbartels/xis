package one.xis.context.arrayfieldinject;

import one.xis.context.AppContextInitializer;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static one.xis.utils.lang.CollectionUtils.findElementOfType;
import static org.assertj.core.api.Assertions.assertThat;

class ArrayFieldInjectITCase {


    @Test
    void arrayField() {

        AppContextInitializer initializer = new AppContextInitializer(getClass());
        initializer.run();

        Set<Object> singletons = initializer.getSingletons();

        Comp1 comp1 = findElementOfType(singletons, Comp1.class);
        Comp2 comp2 = findElementOfType(singletons, Comp2.class);
        Comp3 comp3 = findElementOfType(singletons, Comp3.class);

        assertThat(comp1).isNotNull();
        assertThat(comp2).isNotNull();
        assertThat(comp3).isNotNull();

        assertThat(comp1.getField()).containsExactlyInAnyOrder(comp2, comp3);

    }
}
