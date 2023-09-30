package one.xis.context.initmethod;

import one.xis.context.AppContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class InitMethodTest {

    @Test
    @DisplayName("Init-method is executed when all fields are present")
    void test() {
        Comp1 comp1 = AppContext.getInstance(getClass()).getSingleton(Comp1.class);
        assertThat(comp1.getResult()).isEqualTo(8);
    }

}
