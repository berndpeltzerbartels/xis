package one.xis.context;

import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

class AppContextInitializerArrayFieldITCase {


    @Test
    void arrayField() {
        var appContext = new TestContextBuilder().withSingletonClasses(Comp1.class, Comp2.class, Comp3.class)
                .withComponentAnnotation(XISComponent.class)
                .build();

        Comp1 comp1 = appContext.getSingleton(Comp1.class);
        Comp2 comp2 = appContext.getSingleton(Comp2.class);
        Comp3 comp3 = appContext.getSingleton(Comp3.class);

        assertThat(comp1.arr.length).isEqualTo(2);
        assertThat(Arrays.asList(comp1.arr)).contains(comp2, comp3);


    }

    interface Interf1 {

    }


    @XISComponent
    static class Comp1 {

        @XISInject
        Interf1[] arr;
    }

    @XISComponent
    static class Comp2 implements Interf1 {

    }

    @XISComponent
    static class Comp3 implements Interf1 {

    }
}