package one.xis.context;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Set;

import static one.xis.utils.lang.CollectionUtils.findElementOfType;
import static org.assertj.core.api.Assertions.assertThat;

class AppContextInitializerArrayFieldITCase {


    @Test
    void arrayField() {
        AppContextInitializer initializer = new AppContextInitializer(new TestReflection(Comp1.class, Comp2.class, Comp3.class));
        initializer.run();

        Set<Object> singletons = initializer.getSingletons();

        Comp1 comp1 = findElementOfType(singletons, Comp1.class);
        Comp2 comp2 = findElementOfType(singletons, Comp2.class);
        Comp3 comp3 = findElementOfType(singletons, Comp3.class);

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