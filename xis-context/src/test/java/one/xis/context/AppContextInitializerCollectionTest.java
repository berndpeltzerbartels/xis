package one.xis.context;

import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.LinkedList;

import static org.assertj.core.api.Assertions.assertThat;

class AppContextInitializerCollectionTest {


    @Test
    void linkedListField() {
        var context = AppContext.builder().withSingletonClasses(Comp1.class, Comp2.class, Comp3.class).build();

        Collection<Object> singletons = context.getSingletons();

        Comp1 comp1 = singletons.stream().filter(Comp1.class::isInstance).map(Comp1.class::cast).findFirst().orElseThrow();
        Comp2 comp2 = singletons.stream().filter(Comp2.class::isInstance).map(Comp2.class::cast).findFirst().orElseThrow();
        Comp3 comp3 = singletons.stream().filter(Comp3.class::isInstance).map(Comp3.class::cast).findFirst().orElseThrow();

        assertThat(comp1.linkedList).hasSize(2);
        assertThat(comp1.linkedList).contains(comp2, comp3);

    }

    @Test
    void linkedListFieldObjects() {
        var context = AppContext.builder()
                .withSingletons(new Comp1(), new Comp2(), new Comp3())
                .build();

        Collection<Object> singletons = context.getSingletons();

        Comp1 comp1 = singletons.stream().filter(Comp1.class::isInstance).map(Comp1.class::cast).findFirst().orElseThrow();
        Comp2 comp2 = singletons.stream().filter(Comp2.class::isInstance).map(Comp2.class::cast).findFirst().orElseThrow();
        Comp3 comp3 = singletons.stream().filter(Comp3.class::isInstance).map(Comp3.class::cast).findFirst().orElseThrow();

        assertThat(comp1.linkedList).hasSize(2);
        assertThat(comp1.linkedList).contains(comp2, comp3);

    }

    interface Interf1 {

    }


    @XISComponent
    static class Comp1 {

        @XISInject
        LinkedList<Interf1> linkedList;
    }

    @XISComponent
    static class Comp2 implements Interf1 {

    }

    @XISComponent
    static class Comp3 implements Interf1 {

    }
}