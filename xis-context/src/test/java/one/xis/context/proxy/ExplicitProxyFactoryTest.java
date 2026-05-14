package one.xis.context.proxy;

import one.xis.context.TestContextBuilder;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ExplicitProxyFactoryTest {

    @Test
    void ignoresExplicitProxyFactoryWithoutScannedProxyInterfaces() {
        var context = new TestContextBuilder()
                .withSingletonClass(TestProxyFactory.class)
                .build();

        assertThat(context.getSingleton(TestProxyFactory.class)).isNotNull();
    }
}
