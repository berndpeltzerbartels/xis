package one.xis.context.all;

import one.xis.context.AppContext;
import one.xis.context.ProxyTargets;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class OverallContextTest {

    @Test
    void test() {
        OverallTimingAdvice.invocations = 0;

        var context = AppContext.builder()
                .withPackage("one.xis.context.all")
                .build();

        assertThat(context.getSingletons()).hasSize(13); // AppContext, EventEmitterImpl and Scheduler are also included

        assertThat(context.getSingleton(Comp1.class)).isNotNull();
        assertThat(context.getSingleton(Comp2.class)).isNotNull();
        assertThat(context.getSingleton(Comp3.class)).isNotNull();
        assertThat(context.getSingleton(Comp4.class)).isNotNull();
        assertThat(context.getSingleton(Comp5.class)).isNotNull();
        assertThat(context.getSingleton(Comp6.class)).isNotNull();
        assertThat(context.getSingleton(AdvisedServiceConsumer.class)).isNotNull();
        assertThat(context.getSingleton(AdvisedBeanConsumer.class)).isNotNull();

        assertThat(context.getSingleton(Comp1.class).getComp2()).isNotNull();
        assertThat(context.getSingleton(Comp1.class).getComp3()).isNotNull();
        assertThat(context.getSingleton(Comp5.class).getComp4()).isNotNull();
        assertThat(context.getSingleton(Comp6.class).getComp5()).isNotNull();
        var advisedTarget = (AdvisedServiceImpl) ProxyTargets.unwrap(context.getSingleton(AdvisedServiceConsumer.class).getAdvisedService());
        assertThat(advisedTarget.getComp2()).isNotNull();
        assertThat(context.getSingleton(AdvisedServiceConsumer.class).describe()).isEqualTo("advised:service:Comp2");
        assertThat(context.getSingleton(AdvisedBeanConsumer.class).describe()).isEqualTo("advised:bean:Comp4");
        assertThat(OverallTimingAdvice.invocations).isEqualTo(2);

    }
}
