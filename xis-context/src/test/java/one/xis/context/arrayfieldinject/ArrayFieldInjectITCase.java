package one.xis.context.arrayfieldinject;

import one.xis.context.AppContext;
import org.junit.jupiter.api.Test;

import static one.xis.utils.lang.CollectionUtils.findElementOfType;
import static org.assertj.core.api.Assertions.assertThat;

class ArrayFieldInjectITCase {


    @Test
    void arrayField() {

        var singletons = AppContext.getInstance(getClass()).getSingletons();

        var comp1 = findElementOfType(singletons, Comp1.class);
        var comp2 = findElementOfType(singletons, Comp2.class);
        var comp3 = findElementOfType(singletons, Comp3.class);

        assertThat(comp1).isNotNull();
        assertThat(comp2).isNotNull();
        assertThat(comp3).isNotNull();

        assertThat(comp1.getField()).containsExactlyInAnyOrder(comp2, comp3);

    }
}
