package one.xis.context.beanmethod;

import one.xis.context.AppContext;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BeanCreationMethodTest {

    @Test
    void test() {
        var context = AppContext.builder()
                .withPackage("one.xis.context.beanmethod")
                .build();

        assertThat(context.getSingleton(Comp1.class)).isNotNull();
        assertThat(context.getSingleton(Comp1.class).getComp2()).isNotNull();
        assertThat(context.getSingleton(Comp2.class)).isNotNull();
        assertThat(context.getSingleton(Comp3.class)).isNotNull();
        assertThat(context.getSingleton(Comp3.class).getComp2()).isNotNull();
        assertThat(context.getSingleton(AppContext.class)).isNotNull();
        assertThat(context.getSingletons()).hasSize(4);
    }
}
