package one.xis.context.emptycollparam;

import one.xis.context.AppContextBuilder;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class EmptyCollectionParamTest {

    @Test
    void testInitWithObject() {
        var context = AppContextBuilder.createInstance()
                .withPackage(EmptyCollectionParamTest.class.getPackageName())
                .build();
        var comp = context.getSingleton(Comp.class);
        assertThat(comp).isNotNull();
        assertThat(comp.getComponents()).isNotNull();
        assertThat(comp.getComponents()).isEmpty();
    }
}
