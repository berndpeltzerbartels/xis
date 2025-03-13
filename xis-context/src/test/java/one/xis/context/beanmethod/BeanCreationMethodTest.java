package one.xis.context.beanmethod;

import one.xis.context.AppContext;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.assertj.core.api.Assertions.assertThat;

class BeanCreationMethodTest {
    private static final Logger logger = LoggerFactory.getLogger(BeanCreationMethodTest.class);

    @Test
    void test() {
        logger.info("Starting test");

        var context = AppContext.builder()
                .withPackage("one.xis.context.beanmethod")
                .build();

        logger.info("Context built");

        assertThat(context.getSingleton(Comp1.class)).isNotNull();
        logger.info("Comp1 is not null");

        assertThat(context.getSingleton(Comp1.class).getComp2()).isNotNull();
        logger.info("Comp1.getComp2() is not null");

        assertThat(context.getSingleton(Comp2.class)).isNotNull();
        logger.info("Comp2 is not null");

        assertThat(context.getSingleton(Comp3.class)).isNotNull();
        logger.info("Comp3 is not null");

        assertThat(context.getSingleton(Comp3.class).getComp2()).isNotNull();
        logger.info("Comp3.getComp2() is not null");

        assertThat(context.getSingleton(AppContext.class)).isNotNull();
        logger.info("AppContext is not null");

        assertThat(context.getSingletons()).hasSize(4);
        logger.info("Singletons size is 4");
    }
}
