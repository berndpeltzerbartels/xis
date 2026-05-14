package one.xis.context.interfacewithdefault;

import one.xis.context.AppContext;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class InterfWithDefaultTest {

    @Test
    void test() {
        var context = AppContext.builder()
                .withPackage("one.xis.context.interfacewithdefault")
                .build();

        var fieldHolder = context.getSingleton(FieldHolder.class);
        assertThat(fieldHolder).isNotNull();
        assertThat(fieldHolder.getFieldByConstructor()).hasSize(1);
        assertThat(fieldHolder.getInjectedField()).hasSize(1);
        assertThat(fieldHolder.getFieldByConstructor().get(0)).isInstanceOf(Comp.class);
        assertThat(fieldHolder.getInjectedField().get(0)).isInstanceOf(Comp.class);
        assertThat(context.getSingleton(Interf.class)).isInstanceOf(Comp.class);

    }
}
