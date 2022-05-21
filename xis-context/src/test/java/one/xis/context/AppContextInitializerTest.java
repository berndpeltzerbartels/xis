package one.xis.context;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class AppContextInitializerTest {

    @Test
    void run() {
        AppContextInitializer initializer = new AppContextInitializer(getClass());
        initializer.run();

        Set<Object> singletons = initializer.getSingletons();

        Comp1 comp1 = singletons.stream().filter(Comp1.class::isInstance).map(Comp1.class::cast).findFirst().orElseThrow();
        Comp2 comp2 = singletons.stream().filter(Comp2.class::isInstance).map(Comp2.class::cast).findFirst().orElseThrow();
        Comp3 comp3 = singletons.stream().filter(Comp3.class::isInstance).map(Comp3.class::cast).findFirst().orElseThrow();

        assertThat(comp1.comp2).isEqualTo(comp2);
        assertThat(comp2.comp1).isEqualTo(comp1);
        assertThat(comp3.comp2).isEqualTo(comp2);
    }


    @Comp
    static class Comp1 {

        @Inj
        Comp2 comp2;

        int value;

        @Init
        void init() {
            value = comp2.testValue();
        }
    }

    @Comp
    static class Comp2 {

        @Inj
        Comp1 comp1;

        int testValue() {
            return 2;
        }
    }

    @Comp
    static class Comp3 {
        private final Comp2 comp2;

        Comp3(Comp2 comp2) {
            this.comp2 = comp2;
        }
    }
}