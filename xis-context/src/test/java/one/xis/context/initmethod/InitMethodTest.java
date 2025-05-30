package one.xis.context.initmethod;

import one.xis.context.AppContext;
import one.xis.context.AppContextBuilder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class InitMethodTest {

    @Nested
    class AppContextInitMethodTest {
        @Test
        @DisplayName("Init-method is executed when all fields are present and bean class is instantiated")
        void testInitWithClassInstantiation() {
            Comp1 comp1 = AppContext.getInstance(getClass()).getSingleton(Comp1.class);
            assertThat(comp1.getResult()).isEqualTo(8);
        }

        @Test
        @DisplayName("Init-method is executed when all fields of already instantiated bean are present")
        void testInitWithObject() {
            var context = AppContextBuilder.createInstance()
                    .withSingletons(Comp2.class)
                    .withSingleton(new Comp1(new Comp3()))
                    .build();
            var comp1 = context.getSingleton(Comp1.class);
            assertThat(comp1.getComp2()).isNotNull();
            assertThat(comp1.getComp3()).isNotNull();
            assertThat(comp1.getResult()).isEqualTo(8);
        }
    }

    @Nested
    class IntegrationTestContestInitMethodTest {

        @Test
        @DisplayName("Init-method is executed when all fields are present and bean class is instantiated")
        void testInitWithClassInstantiation() {
            var context = AppContextBuilder.createInstance().withBasePackageClass(getClass()).build();
            var comp1 = context.getSingleton(Comp1.class);
            assertThat(comp1.getResult()).isEqualTo(8);
        }


    }


}
