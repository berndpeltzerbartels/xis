package one.xis.context.all;

import one.xis.context.AppContext;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class OverallContextTest {

    @Test
    void test() {
        var context = AppContext.builder()
                .withPackage("one.xis.context.all")
                .build();

        assertThat(context.getSingletons()).hasSize(7);

        assertThat(context.getSingleton(Comp1.class)).isNotNull();
        assertThat(context.getSingleton(Comp2.class)).isNotNull();
        assertThat(context.getSingleton(Comp3.class)).isNotNull();
        assertThat(context.getSingleton(Comp4.class)).isNotNull();
        assertThat(context.getSingleton(Comp5.class)).isNotNull();
        assertThat(context.getSingleton(Comp6.class)).isNotNull();

        assertThat(context.getSingleton(Comp1.class).getComp2()).isNotNull();
        assertThat(context.getSingleton(Comp1.class).getComp3()).isNotNull();
        assertThat(context.getSingleton(Comp5.class).getComp4()).isNotNull();
        assertThat(context.getSingleton(Comp6.class).getComp5()).isNotNull();

    }
}
