package one.xis.context.arrayfieldconstructor;

import one.xis.context.AppContext;
import org.junit.jupiter.api.Test;

import static one.xis.utils.lang.CollectionUtils.findElementOfType;
import static org.assertj.core.api.Assertions.assertThat;

class ArrayDependencyFieldConstructorTest {


    @Test
    void arrayField() {

        var singletons = AppContext.getInstance(getClass()).getSingletons();

        var componentWithArrayField = findElementOfType(singletons, ComponentWithArrayField.class);
        var comp2 = findElementOfType(singletons, Comp2.class);
        var comp1 = findElementOfType(singletons, Comp1.class);

        assertThat(componentWithArrayField).isNotNull();
        assertThat(comp2).isNotNull();
        assertThat(comp1).isNotNull();

        assertThat(componentWithArrayField.getField()).containsExactlyInAnyOrder(comp2, comp1);

    }
}
