package one.xis.context.proxy;

import one.xis.context.TestContextBuilder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ProxyTest {

    @Test
    @DisplayName("Create a proxy and invoke a proxy-method")
    void test() {
        var context = new TestContextBuilder()
                .withPackage("one.xis.context.proxy")
                .build();

        var proxy = context.getSingleton(TestInterface.class);
        var result = proxy.add(1, 3);

        assertThat(result).isEqualTo(4);
    }
}
