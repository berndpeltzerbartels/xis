package one.xis.context;

import one.xis.context.proxy.TestInterface;
import one.xis.context.proxy.TestInvocationHandler;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ProxyUtilTest {

    @Test
    void invocationHandlerClasses() {
        var result = ProxyUtil.invocationHandlerClasses(TestInterface.class);
        assertThat(result).isEqualTo(List.of(TestInvocationHandler.class));
    }

}