package one.xis.context;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ConstructorInstantiatorTest {

    @Test
    @DisplayName("In case of a non static inner class use the 'real' constructor with parent class as a parameter")
    void noStaticInnerClass() {
        var context = AppContext.builder()
                .withSingletonClass(InnerTestClass.class)
                .withSingleton(this)
                .build();

        assertThat(context.getSingletons(InnerTestClass.class)).isNotNull();
    }


    class InnerTestClass {

    }

}