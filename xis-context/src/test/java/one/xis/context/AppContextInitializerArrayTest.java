package one.xis.context;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class AppContextInitializerArrayTest {


    @Test
    void arrayField() {
        AppContextInitializer initializer = new AppContextInitializer(new TestReflection(Comp1.class, Comp2.class, Comp3.class));
        initializer.run();

        Set<Object> singletons = initializer.getSingletons();

        Comp1 comp1 = singletons.stream().filter(Comp1.class::isInstance).map(Comp1.class::cast).findFirst().orElseThrow();
        Comp2 comp2 = singletons.stream().filter(Comp2.class::isInstance).map(Comp2.class::cast).findFirst().orElseThrow();
        Comp3 comp3 = singletons.stream().filter(Comp3.class::isInstance).map(Comp3.class::cast).findFirst().orElseThrow();

        assertThat(comp1.arr.length).isEqualTo(2);
        assertThat(Arrays.asList(comp1.arr)).contains(comp2, comp3);


    }

    interface Interf1 {

    }


    @Comp
    static class Comp1 {

        @Inj
        Interf1[] arr;
    }

    @Comp
    static class Comp2 implements Interf1 {

    }

    @Comp
    static class Comp3 implements Interf1 {

    }
}