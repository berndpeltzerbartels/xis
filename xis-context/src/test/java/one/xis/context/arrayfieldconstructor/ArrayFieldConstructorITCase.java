package one.xis.context.arrayfieldconstructor;

import one.xis.context.AppContext;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static one.xis.utils.lang.CollectionUtils.findElementOfType;
import static org.assertj.core.api.Assertions.assertThat;

class ArrayFieldConstructorITCase {


    @Test
    void arrayField() {
        
        Set<Object> singletons = AppContext.getInstance(getClass()).getSingletons();

        ComponentWithArrayField componentWithArrayField = findElementOfType(singletons, ComponentWithArrayField.class);
        Comp2 comp2 = findElementOfType(singletons, Comp2.class);
        Comp1 comp1 = findElementOfType(singletons, Comp1.class);

        assertThat(componentWithArrayField).isNotNull();
        assertThat(comp2).isNotNull();
        assertThat(comp1).isNotNull();

        assertThat(componentWithArrayField.getField()).containsExactlyInAnyOrder(comp2, comp1);

    }
}
