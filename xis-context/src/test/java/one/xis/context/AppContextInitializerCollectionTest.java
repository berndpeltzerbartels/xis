package one.xis.context;

import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.LinkedList;

import static org.assertj.core.api.Assertions.assertThat;

class AppContextInitializerCollectionTest {


    @Test
    void linkedListField() {
        var context = AppContext.builder().withSingletonClasses(Comp11.class, Comp12.class, Comp13.class).build();

        Collection<Object> singletons = context.getSingletons();

        Comp11 comp11 = singletons.stream().filter(Comp11.class::isInstance).map(Comp11.class::cast).findFirst().orElseThrow();
        Comp12 comp12 = singletons.stream().filter(Comp12.class::isInstance).map(Comp12.class::cast).findFirst().orElseThrow();
        Comp13 comp13 = singletons.stream().filter(Comp13.class::isInstance).map(Comp13.class::cast).findFirst().orElseThrow();

        //   assertThat(comp11.linkedList).hasSize(2);
        // assertThat(comp11.linkedList).contains(comp12, comp13);

    }

    @Test
    void linkedListFieldObjects() {
        var context = AppContext.builder()
                .withSingletons(new Comp11(), new Comp12(), new Comp13())
                .build();

        Collection<Object> singletons = context.getSingletons();

        Comp11 comp11 = singletons.stream().filter(Comp11.class::isInstance).map(Comp11.class::cast).findFirst().orElseThrow();
        Comp12 comp12 = singletons.stream().filter(Comp12.class::isInstance).map(Comp12.class::cast).findFirst().orElseThrow();
        Comp13 comp13 = singletons.stream().filter(Comp13.class::isInstance).map(Comp13.class::cast).findFirst().orElseThrow();

        assertThat(comp11.linkedList).hasSize(2);
        assertThat(comp11.linkedList).contains(comp12, comp13);

    }

    interface Interf1 {

    }


    @XISComponent
    static class Comp11 {

        @XISInject
        LinkedList<Interf1> linkedList;
    }

    @XISComponent
    static class Comp12 implements Interf1 {

    }

    @XISComponent
    static class Comp13 implements Interf1 {

    }
}